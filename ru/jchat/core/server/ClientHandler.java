package ru.jchat.core.server;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//для отключения клиента не авторизовавшегося за 120 сек
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private final int waitAuthDelay;
    private boolean isAuthTimeBreack;//true если время ожидания аутентификации истекло
    private AuthResult authResult;
    
    private Timer authWaitTimer;
    
    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        this.waitAuthDelay = 120000;
        this.isAuthTimeBreack = false;
        this.authWaitTimer = null;
        
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try{
                    while(true){                 
                        if (in.available() > 0){//проверяю наличие входного пакета чтобы не заблокироваться на in.readUTF
                            //Подключился клиент ждёт аутентификации
                            authResult = getAuthResult(in.readUTF());
                            sendMsg(authResult.msg);
                            if (authResult.res == 1) {  //authok 
                                if (this.authWaitTimer != null) this.authWaitTimer.cancel();
                                nick = authResult.Nick;
                                server.subscribe(this);
                                break;
                            } else {//в сокет записалось что-то отличное от пары логин/пароль или с ошибкой
                                if (this.authWaitTimer == null) { //таймер ещё не создан
                                    System.out.printf("Создал таймер");
                                    this.authWaitTimer = new Timer();
                                    ClientHandler self = this;
                                    this.authWaitTimer.schedule(new TimerTask() {
                                        @Override
                                            public void run() {
                                                self.isAuthTimeBreack = true;
                                                System.out.printf("Таймер сработал");
                                            }
                                    }, self.waitAuthDelay);   
                                }
                            }
                        } else if (this.isAuthTimeBreack) {
                            System.out.printf("Время ожидания истекло");
                            sendMsg("Время ожидания истекло"); 
                            chekAuthResul();//выбрасывает AuthTimeOutException для закрытия соединения
                        }
                    }
                    while(true){
                        String msg = in.readUTF();
                        System.out.println(nick + ": " + msg);
                        if (msg.startsWith("/")){
                            if (msg.equals("/end")) break;
                            if (msg.startsWith("/w ")){ // /w nick1 hello java
                                String[] data = msg.split("\\s", 3);
                                server.sendPrivateMsg(this, data[1], data[2]);
                            }
                        } else {
                            server.broadcastMsg(nick + ": " + msg);
                        }
                    }
                }catch (AuthTimeOutException | IOException e){
                    System.out.println("catch:"+e);
                    e.printStackTrace();
                }finally {
                    System.out.println("finally");
                    nick = null;
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     *
     */
    public class AuthResult {
        public String Nick;//имя юзера
        public int res ;//код результата
        public String msg ;//сообщение
        AuthResult (){
            this.Nick  = null;//имя юзера
            this.res   =  0;//код результата
            this.msg   = "Непредвиденная ошибка";//сообщение
        }
    }

    /**
     * 
     * @param inMsg сообщени от клиента (должно содержать пару логин/пароль)
     * @return возвращает класс AuthResult
     *  <b> public class AuthResult </b> <p>{
                        public String Nick;//имя юзера <br>
                        public int res ;//код результата <br>
                        public String msg ;//сообщение <br>
     *              }
                </p>
     */
    public AuthResult getAuthResult(String inMsg) {
        AuthResult authResult = new AuthResult();
        if (inMsg.startsWith("/auth ")){
            String[] data = inMsg.split("\\s");
            if (data.length == 3){
                String newNick = server.getAuthService().getNickByLoginAndPass(data[1], data[2]);
                if (newNick != null){
                    if (!server.isNickBusy(newNick)){
                        authResult.Nick = newNick;
                        authResult.msg = "/authok " + newNick;
                        authResult.res = 1;
                        return   authResult;
                    } else {
                        authResult.Nick = newNick;
                        authResult.msg = "Учетная запись уже занята";
                        authResult.res = 2;
                        return   authResult;
                    }
                } else {
                    authResult.Nick = null;
                    authResult.msg = "Неверный логин/пароль";
                    authResult.res = 3;
                    return   authResult;
                }
            } else {
                    authResult.Nick = null;
                    authResult.msg = "Не передан логин или пароль";
                    authResult.res = 4;
                    return   authResult;                
            }
        }
        return authResult;
    }
    
    /**
     * <p> Если this.isAuthTimeBreack = true, то выбрасывает AuthTimeOutException для закрытия соединения </p>
     * @return
     * @throws AuthTimeOutException
     */
    public boolean chekAuthResul() throws AuthTimeOutException{
        if (this.isAuthTimeBreack) throw new AuthTimeOutException("Закончилось время ожидание аутентификации");
        return this.isAuthTimeBreack;
    }
    
    public String getAuthResultMsg(int authRes) {
        switch (authRes) {
            case 1 : return "/authok ";
            case 2 : return "Учетная запись уже занята";
            case 3 : return "Неверный логин/пароль";  
            case 4 : return "Не передан логин или пароль";                 
        }
        return "";
    }
    
    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

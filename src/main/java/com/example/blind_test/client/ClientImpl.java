package com.example.blind_test.client;


import com.example.blind_test.front.controllers.MainMenuController;
import com.example.blind_test.front.models.Game;
import com.example.blind_test.front.models.Player;
import com.example.blind_test.front.models.Question;
import com.example.blind_test.shared.CommunicationTypes;
import com.example.blind_test.shared.FieldsRequestName;
import com.example.blind_test.shared.NetCodes;
import com.example.blind_test.shared.Properties;
import com.example.blind_test.shared.communication.Request;
import com.example.blind_test.shared.communication.Response;
import com.example.blind_test.shared.gson_configuration.GsonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringBufferInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public abstract class ClientImpl {
    protected static final Hashtable<String, Consumer<String>> listOfFunctions = new Hashtable<>();
    private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class);
    private String ipAddress;
    private AsynchronousSocketChannel client;
    private MainMenuController controller;
    private Player player;
    private Question question;

    public static Consumer<String> getFunctionWithRequestCode(Response response) {
        return listOfFunctions.get(response.getNetCode());
    }

    public void initThreadReader() {
        Thread reader = new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(Properties.BUFFER_SIZE);
            try {
                while (client.isOpen()) {
                    int nb = client.read(buffer).get();
                    String jsonRes = new String(buffer.array()).substring(0, nb);
                    logger.info("The received response \n{}", jsonRes);
                    Response response = GsonConfiguration.gson.fromJson(jsonRes, Response.class);
                    ClientImpl.getFunctionWithRequestCode(response).accept(response.getResponse());
                    buffer = ByteBuffer.allocate(Properties.BUFFER_SIZE);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        reader.start();
    }

    public void initListOfFunctions() {
        listOfFunctions.put(NetCodes.CREATE_GAME_SUCCEED, this::createGameSucceeded);
        listOfFunctions.put(NetCodes.LIST_OF_GAME_NOT_STARTED_SUCCEED, this::listOfNotStartedGameSucceded);
        listOfFunctions.put(NetCodes.MODIFY_SCORE_SUCCEED, this::modifyPlayerScoreSucceded);
        listOfFunctions.put(NetCodes.GET_RESPONSE_FOR_QUESTION_SUCCEED, this::getQuestionResponseSucceded);
        listOfFunctions.put(NetCodes.CHANGE_GAME_STATE_SUCCEED, this::modifyGameStateSucceded);
        listOfFunctions.put(NetCodes.CHANGE_GAME_STATE_FAILED, this::modifyGameStateFailed);
        listOfFunctions.put(NetCodes.GET_RESPONSE_FOR_QUESTION_FAILED, this::getQuestionResponseFailed);
        listOfFunctions.put(NetCodes.MODIFY_SCORE_FAILED, this::modifyPlayerScoreFailed);
        listOfFunctions.put(NetCodes.LIST_OF_GAME_NOT_STARTED_FAILED, this::listOfNotStartedGameFailed);
    }



    public void createGameSucceeded(String responseData) {
        Player player = GsonConfiguration.gson.fromJson(responseData, Player.class);
        this.player = player;
    }

    public void listOfNotStartedGameSucceded(String responseData){
        List<Game> game = GsonConfiguration.gson.fromJson(responseData, CommunicationTypes.mapListGameJsonTypeData);

    }

    public void modifyGameStateSucceded(String responseData){
    Map<String,String>  data = GsonConfiguration.gson.fromJson(responseData, CommunicationTypes.mapJsonTypeData);
    int gameId = Integer.parseInt(data.get(FieldsRequestName.GAME_ID));
    String username = data.get(FieldsRequestName.USERNAME);
    this.player.getGame().setState(true);
    }

    public void modifyPlayerScoreSucceded(String responseData){
        Map<String,String>  data = GsonConfiguration.gson.fromJson(responseData, CommunicationTypes.mapJsonTypeData);
        int gameId = Integer.parseInt(data.get(FieldsRequestName.GAME_ID));
        String username = data.get(FieldsRequestName.USERNAME);
        int score = Integer.parseInt(data.get(FieldsRequestName.PLAYER_SCORE));
        this.player.setScore(score);
    }

    public void getQuestionResponseSucceded(String responseData){
        Map<String, String> data = GsonConfiguration.gson.fromJson(responseData, CommunicationTypes.mapJsonTypeData);
        int gameId = Integer.parseInt(data.get(FieldsRequestName.GAME_ID));
        String username = data.get(FieldsRequestName.USERNAME);
        int idCurrentQuestion = Integer.parseInt(data.get(FieldsRequestName.CURRENT_QUESTION));
        String playerResponse = data.get(FieldsRequestName.PLAYER_RESPONSE);
        int score = Integer.parseInt(data.get(FieldsRequestName.PLAYER_SCORE));
        this.player.getGame().getQuestion(new Question.QuestionBuilder(idCurrentQuestion).build()).setState(true);
    }

    public void listOfNotStartedGameFailed(String responseData){

    }

    public void modifyGameStateFailed(String responseData){

    }

    public void modifyPlayerScoreFailed(String responseData){

    }

    public void getQuestionResponseFailed(String responseData){

    }





    // Functions that send the requests :
    public void createGame(boolean type, boolean state, int rounds, int players, int time_question, int responseTime, String username) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put(FieldsRequestName.IP_ADDRESS, ipAddress);
        requestData.put(FieldsRequestName.GAME_TYPE, String.valueOf(type));
        requestData.put(FieldsRequestName.ROUNDS, String.valueOf(rounds));
        requestData.put(FieldsRequestName.PLAYERS, String.valueOf(players));
        requestData.put(FieldsRequestName.TIME_QUESTION, String.valueOf(time_question));
        requestData.put(FieldsRequestName.STATE, String.valueOf(state));
        requestData.put(FieldsRequestName.USERNAME, username);
        Request createGame = new Request(NetCodes.CREATE_GAME,
                GsonConfiguration.gson.toJson(requestData, CommunicationTypes.mapJsonTypeData));
        request(createGame);
    }

    public void listOfNotStartedGame() {
        Map<String, String> requestData = new HashMap<>();
        requestData.put(FieldsRequestName.IP_ADDRESS, ipAddress);
        Request lisOfNotStartedGame = new Request(NetCodes.LIST_OF_GAME_NOT_STARTED, GsonConfiguration.gson.toJson(requestData, CommunicationTypes.mapJsonTypeData));
        request(lisOfNotStartedGame);
    }

    public void modifyGameState(String username, int gameId) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put(FieldsRequestName.USERNAME, username);
        requestData.put(FieldsRequestName.GAME_ID, String.valueOf(gameId));
        Request modifyGameState = new Request(NetCodes.CHANGE_GAME_STATE, GsonConfiguration.gson.toJson(requestData, CommunicationTypes.mapJsonTypeData));
        request(modifyGameState);
    }

    public void modifyPlayerScore(String username, int gameId, int playerScore) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put(FieldsRequestName.USERNAME, username);
        requestData.put(FieldsRequestName.GAME_ID, String.valueOf(gameId));
        requestData.put(FieldsRequestName.PLAYER_SCORE, String.valueOf(playerScore));
        Request modifyPlayerScore = new Request(NetCodes.CHANGE_GAME_STATE, GsonConfiguration.gson.toJson(requestData, CommunicationTypes.mapJsonTypeData));
        request(modifyPlayerScore);
    }

    public void getQuestionResponse(String username, int gameId, int playerScore, int idCurrentQuestion, String playerResponse){
        Map<String, String> requestData = new HashMap<>();
        requestData.put(FieldsRequestName.USERNAME, username);
        requestData.put(FieldsRequestName.GAME_ID, String.valueOf(gameId));
        requestData.put(FieldsRequestName.PLAYER_SCORE, String.valueOf(playerScore));
        requestData.put(FieldsRequestName.CURRENT_QUESTION, String.valueOf(idCurrentQuestion));
        requestData.put(FieldsRequestName.PLAYER_RESPONSE, playerResponse);
        Request getQuestionResponse = new Request(NetCodes.CHANGE_GAME_STATE, GsonConfiguration.gson.toJson(requestData, CommunicationTypes.mapJsonTypeData));
        request(getQuestionResponse);
    }





    // Functions that don't do sql requests :

    public void setAsynchronousSocketChannel(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public void setMainMenuController(MainMenuController controller) {
        this.controller = controller;
    }

    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public void request(Request request) {
        ByteBuffer buffer = ByteBuffer.wrap(GsonConfiguration.gson.toJson(request).getBytes());
        this.client.write(buffer, buffer, new ClientWriterCompletionHandler());
    }


}

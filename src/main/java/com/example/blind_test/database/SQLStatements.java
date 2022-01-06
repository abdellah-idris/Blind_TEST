package com.example.blind_test.database;

import static com.example.blind_test.database.SQLTablesInformation.*;

public class SQLStatements {

    public static final String GET_RESPONSE_FOR_QUESTION =
            "SELECT " + QUESTION_RESPONSE + " FROM " + QUESTION_TABLE + " WHERE " + QUESTION_ID + "= ? ;";

    public static final String GET_QUESTION =
            "SELECT * FROM " + QUESTION_TABLE + " WHERE " + QUESTION_ID + "= ? ;";

    public static final String INSERT_QUESTION_IN_QUESTION_GAME = "INSERT INTO " + QUESTION_GAME_TABLE + " VALUES (?," +
            "?,?) ;";

    public static final String LIST_OF_GAME_NOT_STARTED = "SELECT * FROM"
            + GAME_TABLE + " WHERE " + GAME_STATE + "=0 ;";

    public static final String CHANGE_GAME_STATE = "UPDATE " + GAME_TABLE + " SET " + GAME_STATE + "= ?" +
            " WHERE " + GAME_ID + "= ?; ";

    public static final String ID_OF_CURRENT_QUESTION = "UPDATE " + GAME_TABLE + " SET "
            + GAME_CURRENT_QUESTION + " = ? " +
            " WHERE " + GAME_ID + "= ?; ";

    private SQLStatements() {
    }


}


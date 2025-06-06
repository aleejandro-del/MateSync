package com.example.matesync.Manager;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;
//clase que manejará mediante SharedPreferences si el usuario ya ha iniciado sesión previamente
public class SharedPreferencesManager {
    private static final String PREF_NAME = "AuthStatus";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_REGISTERED = "isRegistered";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_UID = "userID";
    private static final String KEY_IS_ADMIN = "isAdmin";
    private static final String KEY_USER_GROUP_ID = "userGroupID";
    private static final String KEY_USER_NAME = "userNombre";
    private static final String KEY_GROUP_NAME = "nombreGrupo";

    private static final String KEY_GROUP_INVITE_COD = "codigoInvitacion";

    private static final String KEY_TIMESTAMP = "timestamp";

    // tiempo máximo de vida en milisegundos (por ejemplo, 7 días)
    private static final long MAX_LIFETIME = 7 * 24 * 60 * 60 * 1000; // 7 días
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SharedPreferencesManager(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        this.context = context;
    }


    public void setLoggedIn(boolean isLogged){
        editor.putBoolean(KEY_IS_LOGGED_IN, isLogged);
        editor.putLong(KEY_TIMESTAMP, new Date().getTime());
        editor.apply();
    }
    public void setRegistered(boolean isRegistered){
        editor.putBoolean(KEY_IS_REGISTERED, isRegistered);
        editor.apply();
    }
    public void setUserEmail(String email){
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public void setIsAdmin(boolean isAdmin){
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }
    public void setUserUID(String uid){
        editor.putString(KEY_USER_UID, uid);
        editor.apply();
    }
    public void setUserName(String userName){
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }
    public void setUserGroup(String userGroupID){
        editor.putString(KEY_USER_GROUP_ID, userGroupID);
        editor.apply();
    }
    public void setNombreGrupo(String nombreGrupo){
        editor.putString(KEY_GROUP_NAME, nombreGrupo);
        editor.apply();
    }

    public void setCodigoInvitacion(String codigoInvitacion){
        editor.putString(KEY_GROUP_INVITE_COD, codigoInvitacion);
        editor.apply();
    }
    // Verificar si los datos han expirado
    public boolean isDataExpired() {
        long timestamp = getTimeStamp();
        long currentTime = new Date().getTime();
        return (currentTime - timestamp) > MAX_LIFETIME;
    }
    public long getTimeStamp(){
        return sharedPreferences.getLong(KEY_TIMESTAMP,0);
    }
    public void clearPreferences(){
        editor.clear();
        editor.apply();
    }
    public String getUserGroupID(){
        return sharedPreferences.getString(KEY_USER_GROUP_ID, "");
    }

    public String getUserName(){

        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getNombreGrupo(){

        return sharedPreferences.getString(KEY_GROUP_NAME, "");
    }
    public String getUserEmail(){

        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }
    public String getUserUID(){

        return sharedPreferences.getString(KEY_USER_UID, "");
    }

    public boolean getIsRegistered(){

        return sharedPreferences.getBoolean(KEY_IS_REGISTERED, false);

    }
    public boolean getIsAdmin(){

        return sharedPreferences.getBoolean(KEY_IS_ADMIN, false);

    }
    public boolean getIsLogged(){
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

    }
    public String getICodInv(){
        return sharedPreferences.getString(KEY_GROUP_INVITE_COD, "");

    }
}

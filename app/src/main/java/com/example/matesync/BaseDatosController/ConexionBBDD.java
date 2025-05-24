package com.example.matesync.BaseDatosController;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.matesync.AuthActivities.RegisterActivity;
import com.example.matesync.Modelo.GrupoDomestico;
import com.example.matesync.Modelo.MovimientoEconomico;
import com.example.matesync.Modelo.Producto;
import com.example.matesync.Modelo.Tarea;
import com.example.matesync.Modelo.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConexionBBDD {
    private static ConexionBBDD conn;
    private static FirebaseAuth auth;
    private static FirebaseFirestore db;
    private List<ListenerRegistration> activeListeners;

    private ConexionBBDD() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        activeListeners = new ArrayList<>();
    }

    // Método para limpiar todos los listeners cuando ya no sean necesarios
    public void cleanupListeners() {
        for (ListenerRegistration listener : activeListeners) {
            listener.remove();
        }
        activeListeners.clear();
    }

    //singleton
    public static ConexionBBDD getInstance() {
        if (conn == null) {
            conn = new ConexionBBDD();
        }
        return conn;
    }

    public static FirebaseAuth getAuth() {
        return auth;
    }

    //método para registrar usuario
    public void registerUser(String email, String nombre, String password, RegisterActivity context, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserDataBBDD(user.getUid(), email, nombre, context, callback);
                        }
                    } else {
                        // Error en el registro
                        callback.onFailure(task.getException());
                    }
                });
    }

    //serializar Usuario para meterlo en Firestore
    private void saveUserDataBBDD(String uid, String email, String nombre, RegisterActivity context, AuthCallback callback) {
        Usuario usuario = new Usuario(uid, nombre, "", false, email);
        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    //método para recuperar datos de la BBDD de un user mediante su UID
    public void getUserBBDDdata(String uid, FirestoreUserCallback callback) {
        DocumentReference doc = db.collection("usuarios").document(uid);

        doc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String nombreUser = documentSnapshot.getString("nombre");
                    String email = documentSnapshot.getString("email");
                    String grupoUserID = documentSnapshot.getString("grupoID");
                    boolean isAdmin = documentSnapshot.getBoolean("admin");

                    // Consulta adicional para obtener el nombre del grupo
                    if (grupoUserID != null && !grupoUserID.isEmpty()) {
                        DocumentReference grupoDoc = db.collection("gruposDomesticos").document(grupoUserID);
                        grupoDoc.get().addOnCompleteListener(grupoTask -> {
                            if (grupoTask.isSuccessful()) {
                                DocumentSnapshot grupoSnapshot = grupoTask.getResult();
                                String nombreGrupo = grupoSnapshot.exists() ?
                                        grupoSnapshot.getString("nombreGrupo") : null;

                                callback.onCallback(nombreUser, email, isAdmin, grupoUserID, nombreGrupo);
                            } else {
                                callback.onCallback(nombreUser, email, isAdmin, grupoUserID, null);
                            }
                        });
                    } else {
                        callback.onCallback(nombreUser, email, isAdmin, grupoUserID, null);
                    }
                } else {
                    callback.onCallback(null, null, false, null, null);
                }
            } else {
                callback.onCallback(null, null, false, null, null);
            }
        });
    }

    //método para registrar un Grupo Doméstico en la BBDD
    public void registrarGrupoBBDD(String grupoID, GrupoDomestico grupoDomestico, AuthCallback callback) {
        db.collection("gruposDomesticos").document(grupoID)
                .set(grupoDomestico)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    //método que se ejecutará cuando un usuario introduzca el cód de invitación y se meta a un grupo correctamente
    public void addUserAGrupo(Usuario user, String grupoID, Context context) {
        db.collection("gruposDomesticos")
                .document(grupoID)
                .update("miembros", FieldValue.arrayUnion(user))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Elemento añadido al array correctamente");
                    Toast.makeText(context, "Actualización exitosa", Toast.LENGTH_SHORT).show();

                    FirebaseFirestore.getInstance().collection("usuarios").document(user.getUserID()).update("grupoID", grupoID);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al añadir elemento", e);
                    Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //método para verificar si el código de invitación introducido por el usuario coincide con el existente de algún grupo doméstico
    public void verificarCodigoInvitacion(String codigoInput, CodInvitacionCallback callback) {
        db.collection("gruposDomesticos")
                .whereEqualTo("codigoInvitacion", codigoInput.toUpperCase().trim())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String grupoId = document.getId();
                            String nombreGrupo = document.getString("nombreGrupo");
                            callback.onCodigoValido(grupoId, nombreGrupo);
                        } else {
                            callback.onCodigoNoValido();
                        }
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    //método para registrar una tarea en la BBDD
    public void registrarTareaBBDD(Tarea tarea, Context context, TareaCallback callback) {
        CollectionReference productosRef = db.collection("tareas");
        String tareaID = productosRef.document().getId();
        tarea.setTareaID(tareaID);
        db.collection("tareas").document(tareaID).set(tarea)
                .addOnSuccessListener(aVoid -> callback.onSuccessRegisteringTarea())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    //método para registrar un producto en la BBDD
    public void registrarProductoBBDD(Producto producto, Context context, ProductoCallback callback) {
        CollectionReference productosRef = db.collection("productos");
        String productoID = productosRef.document().getId();
        producto.setProductoID(productoID);
        db.collection("productos").document(productoID).set(producto)
                .addOnSuccessListener(aVoid -> callback.onSuccessRegisteringProducto())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    //método para registrar un movimiento económico en la BBDD
    public void registrarMovEcoBBDD(MovimientoEconomico movimientoEconomico, Context context, MovEcoCallback callback) {
        CollectionReference productosRef = db.collection("movimientosEconomicos");
        String movID = productosRef.document().getId();
        movimientoEconomico.setMovID(movID);
        db.collection("movimientosEconomicos").document(movID).set(movimientoEconomico)
                .addOnSuccessListener(aVoid -> callback.onSuccessRegisteringMovimiento())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    //recuperar miembros del grupo de la BBDD para mostrarlos en su recycler view (con listener en tiempo real)
    public void recuperarMiembrosGrupo(String grupoID, Context context, MiembrosCallback callback) {
        ListenerRegistration listener = db.collection("gruposDomesticos")
                .whereEqualTo("grupoID", grupoID)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Usuario> listaUsuarios = new ArrayList<>();
                        DocumentSnapshot documento = querySnapshot.getDocuments().get(0);
                        List<Map<String, Object>> miembros = (List<Map<String, Object>>) documento.get("miembros");

                        if (miembros != null) {
                            for (Map<String, Object> miembro : miembros) {
                                String userID = (String) miembro.get("userID");
                                String nombre = (String) miembro.get("nombre");
                                String email = (String) miembro.get("email");
                                boolean isAdmin = (Boolean) miembro.get("admin");
                                Usuario usuario = new Usuario(userID, nombre, grupoID, isAdmin, email);
                                listaUsuarios.add(usuario);
                            }
                            callback.onRecoverMiembrosSuccess(listaUsuarios);
                        }
                    }
                });
        activeListeners.add(listener);
    }

    //método para recuperar las tareas de cada grupo doméstico desde la BBDD (con listener en tiempo real)
    public void recuperarTareasGrupo(String grupoID, Context context, TareaCallback callback) {
        ListenerRegistration listener = db.collection("tareas")
                .whereEqualTo("grupoID", grupoID)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    List<Tarea> listaTareas = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String nombreTarea = document.getString("nombre");
                            String descripcionTarea = document.getString("descripcion");
                            String userID = document.getString("userID");
                            boolean isDone = Boolean.TRUE.equals(document.getBoolean("isDone"));

                            Tarea tarea = new Tarea(document.getId(), userID, grupoID, nombreTarea, descripcionTarea, isDone);
                            listaTareas.add(tarea);
                        }
                        callback.onSuccessRecoveringTareas(listaTareas);
                    }
                });
        activeListeners.add(listener);
    }

    //método para recuperar los productos de la lista de la compra de cada grupo doméstico desde la BBDD (con listener en tiempo real)
    public void recuperarProductosGrupo(String grupoID, Context context, ProductoCallback callback) {
        ListenerRegistration listener = db.collection("productos")
                .whereEqualTo("grupoID", grupoID)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    List<Producto> listaProductos = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            try {
                                String nombre = document.getString("nombre");
                                String descripcion = document.getString("descripcion");
                                String userID = document.getString("userID");

                                int cantidad = 0;
                                Object cantidadObj = document.get("cantidad");
                                if (cantidadObj != null) {
                                    if (cantidadObj instanceof Long) {
                                        cantidad = ((Long) cantidadObj).intValue();
                                    } else if (cantidadObj instanceof String) {
                                        cantidad = Integer.parseInt((String) cantidadObj);
                                    } else if (cantidadObj instanceof Integer) {
                                        cantidad = (Integer) cantidadObj;
                                    }
                                }

                                Producto producto = new Producto(document.getId(), userID, grupoID, nombre, descripcion, cantidad);
                                listaProductos.add(producto);
                            } catch (Exception ex) {
                                Log.e("Firestore", "Error procesando documento: " + document.getId(), ex);
                            }
                        }
                        callback.onSuccessRecoveringProductos(listaProductos);
                    }
                });
        activeListeners.add(listener);
    }

    public void recuperarMovimientosGrupo(String grupoID, Context context, MovEcoCallback callback) {
        ListenerRegistration listener = db.collection("movimientosEconomicos")
                .whereEqualTo("grupoID", grupoID)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error en listener movimientos", e);
                        callback.onFailure(e);
                        return;
                    }

                    if (querySnapshot == null) {
                        Log.d("Firestore", "Movimientos snapshot es null");
                        callback.onSuccessRecoveringMovimientos(new ArrayList<>());
                        return;
                    }

                    List<MovimientoEconomico> listaMovimientos = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            Log.d("Firestore", "Procesando movimiento: " + document.getId());

                            String conceptoMov = document.getString("concepto");
                            String userID = document.getString("userID");
                            String tipoMov = document.getString("tipo");

                            // Conversión más robusta del valor
                            float valor = 0f;
                            Object valorObj = document.get("valor");
                            if (valorObj != null) {
                                if (valorObj instanceof Double) {
                                    valor = ((Double) valorObj).floatValue();
                                } else if (valorObj instanceof Long) {
                                    valor = ((Long) valorObj).floatValue();
                                } else if (valorObj instanceof Integer) {
                                    valor = ((Integer) valorObj).floatValue();
                                } else if (valorObj instanceof String) {
                                    try {
                                        valor = Float.parseFloat((String) valorObj);
                                    } catch (NumberFormatException ex) {
                                        Log.e("Firestore", "Error parseando valor", ex);
                                    }
                                }
                            }

                            MovimientoEconomico movimiento = new MovimientoEconomico(
                                    document.getId(), grupoID, userID, conceptoMov, tipoMov, valor);
                            listaMovimientos.add(movimiento);
                        } catch (Exception ex) {
                            Log.e("Firestore", "Error procesando documento: " + document.getId(), ex);
                        }
                    }
                    Log.d("Firestore", "Movimientos recuperados: " + listaMovimientos.size());
                    callback.onSuccessRecoveringMovimientos(listaMovimientos);
                });
        activeListeners.add(listener);
    }

    //interfaces de callback
    public interface FirestoreUserCallback {
        void onCallback(String nombreUser, String email, boolean isAdmin, String grupoID, String nombreGrupo);
    }

    public interface CodInvitacionCallback {
        void onCodigoValido(String grupoId, String nombreGrupo);
        void onCodigoNoValido();
        void onError(Exception e);
    }

    public interface TareaCallback {
        void onSuccessRecoveringTareas(List<Tarea> listaTareas);
        void onSuccessRegisteringTarea();
        void onFailure(Exception e);
    }

    public interface ProductoCallback {
        void onSuccessRecoveringProductos(List<Producto> listaProductos);
        void onSuccessRegisteringProducto();
        void onFailure(Exception e);
    }

    public interface MovEcoCallback {
        void onSuccessRecoveringMovimientos(List<MovimientoEconomico> listaMovimientos);
        void onSuccessRegisteringMovimiento();
        void onFailure(Exception e);
    }

    public interface MiembrosCallback {
        void onRecoverMiembrosSuccess(List<Usuario> listaUsuarios);
        void onFailure(Exception exception);
    }

    public interface AuthCallback {
        void onSuccess();
        void onFailure(Exception exception);
    }
}
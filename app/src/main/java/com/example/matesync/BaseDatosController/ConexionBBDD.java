package com.example.matesync.BaseDatosController;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.matesync.AuthActivities.RegisterActivity;
import com.example.matesync.Modelo.GrupoDomestico;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConexionBBDD {
    private static ConexionBBDD conn;
    private static FirebaseAuth auth;
    private static FirebaseFirestore db;

    private ConexionBBDD() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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

                    System.out.println(nombreUser);
                    System.out.println(email);
                    System.out.println(grupoUserID);
                    System.out.println(isAdmin);
                    System.out.println(uid);
                    // Consulta adicional para obtener el nombre del grupo
                    DocumentReference grupoDoc = db.collection("gruposDomesticos").document(grupoUserID);
                    grupoDoc.get().addOnCompleteListener(grupoTask -> {
                        if (grupoTask.isSuccessful()) {
                            DocumentSnapshot grupoSnapshot = grupoTask.getResult();
                            String nombreGrupo = grupoSnapshot.exists() ?
                                    grupoSnapshot.getString("nombreGrupo") : null;

                            callback.onCallback(nombreUser, email, isAdmin, grupoUserID, nombreGrupo);  // Devuelve los datos recogidos de la BBDD usando el callback
                        } else {
                            System.out.println("Error al obtener grupo: " + grupoTask.getException());
                            callback.onCallback(nombreUser, email, isAdmin, grupoUserID, null);
                        }
                    });

                } else {
                    System.out.println("El documento no existe");
                    callback.onCallback(null, null, false, null, null); // Devuelve null si el documento no existe
                }
            } else {
                System.out.println("Error al obtener el documento: " + task.getException());
                callback.onCallback(null, null, false, null, null); // Devuelve null si hay un error
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
                //se pone la condición de la query
                .whereEqualTo("codigoInvitacion", codigoInput.toUpperCase().trim())
                .limit(1) // Solo necesitamos un documento que coincida
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // Encontramos un grupo con este código
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String grupoId = document.getId();
                            String nombreGrupo = document.getString("nombreGrupo");

                            // se devuelve el id y nombre del grupo encontrado
                            callback.onCodigoValido(grupoId, nombreGrupo);
                        } else {
                            // No se encontró ningún grupo con este código
                            callback.onCodigoNoValido();
                        }
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void registrarTareaBBDD(Tarea tarea, Context context, TareaCallback callback) {
        CollectionReference productosRef = db.collection("tareas");
        String tareaID = productosRef.document().getId();
        tarea.setTareaID(tareaID);
        db.collection("tareas").document(tareaID).set(tarea);
    }

    public void registrarProductoBBDD(Producto producto, Context context, ProductoCallback callback) {
        CollectionReference productosRef = db.collection("productos");
        String productoID = productosRef.document().getId();
        producto.setProductoID(productoID);
        db.collection("productos").document(productoID).set(producto);
    }

    //recuperar miembros del grupo de la BBDD para mostrarlos en su recycler view
    public void recuperarMiembrosGrupo(String grupoID, Context context, MiembrosCallback callback) {
        db.collection("gruposDomesticos").whereEqualTo("grupoID", grupoID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (!querySnapshot.isEmpty()) {
                    List<Usuario> listaUsuarios = new ArrayList<>();
                    // Solo debería haber un documento con ese grupoID
                    DocumentSnapshot documento = querySnapshot.getDocuments().get(0);

                    // Obtener el array de miembros
                    List<Map<String, Object>> miembros = (List<Map<String, Object>>) documento.get("miembros");
                    if (miembros != null) {
                        for (Map<String, Object> miembro : miembros) {
                            String userID = (String) miembro.get("userID");
                            String nombre = (String) miembro.get("nombre");
                            String email = (String) miembro.get("email");
                            boolean isAdmin = (boolean) miembro.get("admin");
                            // Añade aquí otros campos que tengas en el objeto miembro

                            Usuario usuario = new Usuario(userID, nombre, grupoID, isAdmin, email); // Ajusta el constructor según tu clase Usuario
                            System.out.println(userID);
                            listaUsuarios.add(usuario);
                        }
                        callback.onRecoverMiembrosSuccess(listaUsuarios);
                    }

                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    //método para recuperar las tareas de cada grupo doméstico desde la BBDD
    public void recuperarTareasGrupo(String grupoID, Context context, TareaCallback callback) {
        db.collection("tareas").whereEqualTo("grupoID", grupoID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (!querySnapshot.isEmpty()) {
                    List<Tarea> listaTareas = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String nombreTarea = document.getString("nombre");
                        String descripcionTarea = document.getString("descripcion");
                        String userID = document.getString("userID");
                        boolean isDone = Boolean.TRUE.equals(document.getBoolean("isDone"));

                        // Crear objeto Tarea y añadir a la lista
                        Tarea tarea = new Tarea(document.getId(), userID, grupoID, nombreTarea, descripcionTarea, isDone);
                        listaTareas.add(tarea);
                        System.out.println(tarea);
                        // Puedes devolver más información si necesitas
                        callback.onSuccessRecoveringTareas(listaTareas);
                    }
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    public void recuperarProductosGrupo(String grupoID, Context context, ProductoCallback callback) {
        db.collection("productos")
                .whereEqualTo("grupoID", grupoID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<Producto> listaProductos = new ArrayList<>();

                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                try {
                                    String nombreTarea = document.getString("nombre");
                                    String descripcionTarea = document.getString("descripcion");
                                    String userID = document.getString("userID");

                                    // Manejo más robusto del campo cantidad
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

                                    Producto producto = new Producto(
                                            document.getId(),
                                            userID,
                                            grupoID,
                                            nombreTarea,
                                            descripcionTarea,
                                            cantidad
                                    );
                                    listaProductos.add(producto);
                                } catch (Exception e) {
                                    Log.e("Firestore", "Error procesando documento: " + document.getId(), e);
                                }
                            }
                        }
                        // Llamar al callback UNA SOLA VEZ con la lista completa
                        callback.onSuccessRecoveringProductos(listaProductos);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    //interfaces de callback para manejar operaciones de éxito, error y envío y recibimiento de datos
    public interface FirestoreUserCallback {
        void onCallback(String nombreUser, String email, boolean isAdmin, String grupoID, String nombreGrupo);
    }

    //verificar el código de invitación
    public interface CodInvitacionCallback {
        void onCodigoValido(String grupoId, String nombreGrupo);

        void onCodigoNoValido();

        void onError(Exception e);
    }

    // manejar la recuperación de las tareas de un grupo
    public interface TareaCallback {
        void onSuccessRecoveringTareas(List<Tarea> listaTareas);

        void onSuccessRegisteringTarea();

        void onFailure(Exception e);
    }

    // manejar la recuperación de los productos de un grupo
    public interface ProductoCallback {
        void onSuccessRecoveringProductos(List<Producto> listaProductos);

        void onSuccessRegisteringProducto();

        void onFailure(Exception e);
    }

    // manejar la recuperación de los miembros de un grupo
    public interface MiembrosCallback {
        void onRecoverMiembrosSuccess(List<Usuario> listaUsuarios);

        void onFailure(Exception exception);
    }

    // Interfaz para manejar el resultado de la autenticación
    public interface AuthCallback {
        void onSuccess();

        void onFailure(Exception exception);
    }

}

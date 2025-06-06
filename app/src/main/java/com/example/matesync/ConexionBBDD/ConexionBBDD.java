package com.example.matesync.ConexionBBDD;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.matesync.AuthActivities.RegisterActivity;
import com.example.matesync.Callbacks.AuthCallback;
import com.example.matesync.Callbacks.CodInvitacionCallback;
import com.example.matesync.Callbacks.FirestoreUserCallback;
import com.example.matesync.Callbacks.MiembrosCallback;
import com.example.matesync.Callbacks.MovEcoCallback;
import com.example.matesync.Callbacks.ProductoCallback;
import com.example.matesync.Callbacks.TareaCallback;
import com.example.matesync.Manager.SharedPreferencesManager;
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

    //método para registrar usuario en Firestore
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

    public void getUserBBDDdata(String uid, Context context, FirestoreUserCallback callback) {
        DocumentReference userDoc = db.collection("usuarios").document(uid);

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot userSnapshot = task.getResult();
                if (userSnapshot.exists()) {
                    String nombreUser = userSnapshot.getString("nombre");
                    String email = userSnapshot.getString("email");
                    String grupoUserID = userSnapshot.getString("grupoID");
                    Boolean adminObj = userSnapshot.getBoolean("admin");
                    boolean isAdmin = adminObj != null ? adminObj : false;

                    // Si el usuario pertenece a un grupo
                    if (grupoUserID != null && !grupoUserID.isEmpty()) {
                        // Consulta para obtener los datos del grupo
                        DocumentReference grupoDoc = db.collection("gruposDomesticos").document(grupoUserID);
                        grupoDoc.get().addOnCompleteListener(grupoTask -> {
                            if (grupoTask.isSuccessful()) {
                                DocumentSnapshot grupoSnapshot = grupoTask.getResult();
                                String nombreGrupo = grupoSnapshot.exists() ?
                                        grupoSnapshot.getString("nombreGrupo") : null;

                                // Obtener el código de invitación del grupo
                                String codigoInvitacion = grupoSnapshot.exists() ?
                                        grupoSnapshot.getString("codigoInvitacion") : null;

                                // Almacenar el código de invitación en SharedPreferences si existe
                                if (codigoInvitacion != null) {
                                    SharedPreferencesManager shared = new SharedPreferencesManager(context);
                                    shared.setCodigoInvitacion(codigoInvitacion);
                                }

                                callback.onCallback(nombreUser, email, isAdmin, grupoUserID, nombreGrupo);
                            } else {
                                callback.onCallback(nombreUser, email, isAdmin, grupoUserID, null);
                            }
                        });
                    } else {
                        // Usuario no está en ningún grupo
                        callback.onCallback(nombreUser, email, isAdmin, grupoUserID, null);
                    }
                } else {
                    // Usuario no existe en la base de datos
                    callback.onCallback(null, null, false, null, null);
                }
            } else {
                // Error al obtener datos del usuario
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
        // Añadir usuario al array de miembros del grupo
        db.collection("gruposDomesticos")
                .document(grupoID)
                .update("miembros", FieldValue.arrayUnion(user))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Elemento añadido al array correctamente");


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
                            String codigoInvitacionGrupo = document.getString("codigoInvitacion");
                            callback.onCodigoValido(codigoInvitacionGrupo, grupoId, nombreGrupo);
                        } else {
                            callback.onCodigoNoValido();
                        }
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    //método para registrar una tarea en la BBDD (ahora como subcolección)
    public void registrarTareaBBDD(Tarea tarea, TareaCallback callback) {
        String grupoID = tarea.getGrupoID();
        CollectionReference tareasRef = db.collection("gruposDomesticos").document(grupoID).collection("tareas");
        String tareaID = tareasRef.document().getId();
        tarea.setTareaID(tareaID);
        tareasRef.document(tareaID).set(tarea).addOnSuccessListener(aVoid -> callback.onSuccessRegisteringTarea()).addOnFailureListener(e -> callback.onFailure(e));
    }

    //método para registrar un producto en la BBDD (ahora como subcolección)
    public void registrarProductoBBDD(Producto producto, Context context, ProductoCallback callback) {
        String grupoID = producto.getGrupoID();
        CollectionReference productosRef = db.collection("gruposDomesticos").document(grupoID).collection("productos");
        String productoID = productosRef.document().getId();
        producto.setProductoID(productoID);
        productosRef.document(productoID).set(producto)
                .addOnSuccessListener(aVoid -> callback.onSuccessRegisteringProducto())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    //método para registrar un movimiento económico en la BBDD
    public void registrarMovEcoBBDD(MovimientoEconomico movimientoEconomico, Context context, MovEcoCallback callback) {
        String grupoID = movimientoEconomico.getGrupoID();
        CollectionReference movimientosRef = db.collection("gruposDomesticos").document(grupoID).collection("movimientosEconomicos");
        String movID = movimientosRef.document().getId();
        movimientoEconomico.setMovID(movID);
        movimientosRef.document(movID).set(movimientoEconomico)
                .addOnSuccessListener(aVoid -> callback.onSuccessRegisteringMovimiento())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    //recuperar miembros del grupo de la BBDD para mostrarlos en su recycler view
    public void recuperarMiembrosGrupo(String grupoID, Context context, MiembrosCallback callback) {
        ListenerRegistration listener = db.collection("usuarios")
                .whereEqualTo("grupoID", grupoID)  // Filtramos por el grupoID
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }

                    List<Usuario> listaUsuarios = new ArrayList<>();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String userID = document.getString("userID");
                            String nombre = document.getString("nombre");
                            String email = document.getString("email");
                            Boolean adminObj = document.getBoolean("admin");
                            boolean isAdmin = adminObj != null ? adminObj : false;

                            Usuario usuario = new Usuario(
                                    userID != null ? userID : document.getId(), // Usar document ID si userID es null
                                    nombre,
                                    grupoID,
                                    isAdmin,
                                    email
                            );
                            listaUsuarios.add(usuario);
                        }
                    }
                    callback.onRecoverMiembrosSuccess(listaUsuarios);
                });
        activeListeners.add(listener);
    }

    //método para recuperar las tareas de cada grupo doméstico desde la BBDD
    public void recuperarTareasGrupo(String grupoID, TareaCallback callback) {
        ListenerRegistration listener = db.collection("gruposDomesticos").document(grupoID).collection("tareas")
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

    //método para recuperar los productos de la lista de la compra de cada grupo doméstico desde la BBDD
    public void recuperarProductosGrupo(String grupoID, Context context, ProductoCallback callback) {
        ListenerRegistration listener = db.collection("gruposDomesticos").document(grupoID).collection("productos")
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
        ListenerRegistration listener = db.collection("gruposDomesticos").document(grupoID).collection("movimientosEconomicos")
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

    // Método para expulsar/borrar un usuario de un grupo
    public void borrarUsuarioDeGrupo(String grupoID, String userID, Context context, AuthCallback callback) {
        // Primero obtener los datos del usuario para poder removerlo del array
        db.collection("usuarios").document(userID)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        // Crear objeto Usuario para remover del array
                        String nombre = userDoc.getString("nombre");
                        String email = userDoc.getString("email");
                        Boolean adminObj = userDoc.getBoolean("admin");
                        boolean isAdmin = adminObj != null ? adminObj : false;

                        Usuario usuario = new Usuario(userID, nombre, grupoID, isAdmin, email);

                        // Remover usuario del array de miembros del grupo
                        db.collection("gruposDomesticos")
                                .document(grupoID)
                                .update("miembros", FieldValue.arrayRemove(usuario))
                                .addOnSuccessListener(aVoid -> {
                                    // Borrar usuario de la subcolección de usuarios del grupo
                                    db.collection("gruposDomesticos").document(grupoID)
                                            .collection("usuarios").document(userID)
                                            .delete()
                                            .addOnSuccessListener(aVoid2 -> {
                                                // Actualizar el usuario principal removiendo el grupoID
                                                db.collection("usuarios").document(userID)
                                                        .update("grupoID", "", "admin", false)
                                                        .addOnSuccessListener(aVoid3 -> {
                                                            Log.d("Firestore", "Usuario expulsado correctamente del grupo");
                                                            Toast.makeText(context, "Usuario expulsado del grupo", Toast.LENGTH_SHORT).show();
                                                            callback.onSuccess();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firestore", "Error al actualizar usuario principal", e);
                                                            callback.onFailure(e);
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("Firestore", "Error al borrar usuario de subcolección", e);
                                                callback.onFailure(e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error al remover usuario del array", e);
                                    callback.onFailure(e);
                                });
                    } else {
                        Log.e("Firestore", "Usuario no encontrado");
                        callback.onFailure(new Exception("Usuario no encontrado"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener datos del usuario", e);
                    callback.onFailure(e);
                });
    }

    // Método para borrar una tarea
    public void borrarTarea(@NonNull Tarea tarea, TareaCallback callback) {
        db.collection("gruposDomesticos").document(tarea.getGrupoID())
                .collection("tareas").document(tarea.getTareaID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Tarea borrada correctamente");
                    callback.onSuccessRemovingTarea();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al borrar tarea", e);
                    callback.onFailure(e);
                });
    }

    // Método para borrar un producto
    public void borrarProducto(@NonNull Producto producto, ProductoCallback callback) {
        db.collection("gruposDomesticos").document(producto.getGrupoID())
                .collection("productos").document(producto.getProductoID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Producto borrado correctamente");
                    callback.onSuccessRemovingProducto();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al borrar producto", e);
                    callback.onFailure(e);
                });
    }

    // Método para borrar un movimiento económico
    public void borrarMovimientoEconomico(@NonNull MovimientoEconomico movimientoEconomico, Context context, MovEcoCallback callback) {
        db.collection("gruposDomesticos").document(movimientoEconomico.getGrupoID())
                .collection("movimientosEconomicos").document(movimientoEconomico.getMovID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Movimiento económico borrado correctamente");
                    callback.onSuccessRemovingMovimiento();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al borrar movimiento económico", e);
                    Toast.makeText(context, "Error al eliminar movimiento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onFailure(e);
                });
    }
}
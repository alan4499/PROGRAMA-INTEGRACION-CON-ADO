import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.sql.Timestamp;
import java.awt.Font;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Date;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;



public class SistemaAutobuses extends JFrame {


    private JButton btnBienvenida, btnVentas, btnAdmin, btnGerente, btnSalir;
    private JPanel panelSuperior, panelCentral;
    private JLabel lblBienvenida;


    private Connection conexion;



    // Agrega estas variables al inicio de la clase
    private Set<String> idsAutobuses = new HashSet<>();
    private Map<String, String> autobusesConPlacas = new HashMap<>();
    private int asientosFisicos = 0;
    private int capacidadMaximaEstabilidad = 0;



    // Clase Ruta (puede ser interna o externa)
    class Ruta {
        String idRuta;
        String idAutobus;
        // otros campos según necesites
    }

    // Método auxiliar para agregar botones al grid
    private void agregarBotonGrid(JPanel panel, JButton boton, int fila) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(boton, gbc);
    }


    private void mostrarLoginDialog(String tipoUsuario, String tipoBase) {
    JDialog loginDialog = new JDialog(this, "Acceso " + tipoUsuario, true);
    loginDialog.setLayout(new GridLayout(3, 2, 10, 10));
    
    JLabel lblUser = new JLabel("Usuario:");
    JTextField txtUser = new JTextField();
    JLabel lblPass = new JLabel("Contraseña:");
    JPasswordField txtPass = new JPasswordField();
    JButton btnLogin = new JButton("Ingresar");
    JButton btnCancel = new JButton("Cancelar");

    loginDialog.add(lblUser);
    loginDialog.add(txtUser);
    loginDialog.add(lblPass);
    loginDialog.add(txtPass);
    loginDialog.add(btnLogin);
    loginDialog.add(btnCancel);

    loginDialog.setSize(350, 150);
    loginDialog.setLocationRelativeTo(this);

    btnCancel.addActionListener(e -> {
        loginDialog.dispose();
        JOptionPane.showMessageDialog(SistemaAutobuses.this, "Acceso cancelado");
    });

    btnLogin.addActionListener(e -> {
        String usuario = txtUser.getText();
        String contrasena = new String(txtPass.getPassword());

        try {
            if (VerificarUsuarios (usuario, contrasena, tipoBase)) {
                loginDialog.dispose();
                if (tipoBase.equals("admin")) {
                    abrirPanelAdmin();
                } else {
                    abrirPanelGerente();
                }
            } else {
                JOptionPane.showMessageDialog(loginDialog, "Credenciales incorrectas");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(loginDialog, "Error de conexión: " + ex.getMessage());
        }
    });

    loginDialog.setVisible(true);
    }

    
    private void conectarBaseDatos() {
        try {
            String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
            String usuario = "root";
            String contraseña = "";
            
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            System.out.println("Conexión exitosa a la base de datos");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage(), 
                                         "Error de conexión", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean VerificarUsuarios (String usuario, String contrasena, String tipoUsuario) throws SQLException {
    String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
    String user = "root";
    String password = "";
    
    String query = "SELECT COUNT(*) FROM usuarios WHERE usuario = ? AND contrasena = ? AND tipo = ?";
    
    try (Connection conn = DriverManager.getConnection(url, user, password);
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, usuario);
        stmt.setString(2, contrasena);
        stmt.setString(3, tipoUsuario);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    }
    return false;
    }


    // Clase para renderizar el botón en la tabla
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Clase para manejar la edición del botón en la tabla
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;
        private SistemaAutobuses sistema;
        
        public ButtonEditor(JCheckBox checkBox, JTable table, SistemaAutobuses sistema) {
            super(checkBox);
            this.table = table;
            this.sistema = sistema;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Obtener los datos de la fila - USAR ÍNDICES CORRECTOS
                int idViaje = (Integer) table.getValueAt(table.getSelectedRow(), 0);
                String idAutobus = table.getValueAt(table.getSelectedRow(), 1).toString();  // Columna 1: Autobús
                String idRuta = table.getValueAt(table.getSelectedRow(), 7).toString();     // Columna 7: ID Ruta
                double precio = (Double) table.getValueAt(table.getSelectedRow(), 6);       // Columna 6: Precio
                
                // Llamar al método para procesar la compra
                sistema.procesarCompraBoleto(idViaje, idAutobus, idRuta, precio);
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
        
        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    private boolean existeIdEnBD(String id) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT COUNT(*) FROM autobuses WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(id));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    private boolean existePlacasEnBD(String placas) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT COUNT(*) FROM autobuses WHERE placas = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, placas.toUpperCase());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean registrarAutobusEnBD(String id, String placas) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "INSERT INTO autobuses (id, placas) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(id));
            stmt.setString(2, placas.toUpperCase());
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Métodos auxiliares para obtener datos de la base de datos
    private List<Object[]> obtenerAutobusesDesdeBD() {
        List<Object[]> autobuses = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT id, placas FROM autobuses ORDER BY id";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] fila = new Object[2];
                fila[0] = rs.getString("id");
                fila[1] = rs.getString("placas");
                autobuses.add(fila);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelCentral, "Error al cargar autobuses: " + ex.getMessage());
        }
        
        return autobuses;
    }


    private void actualizarColeccionesLocales(List<Object[]> datosAutobuses) {
        idsAutobuses.clear();
        autobusesConPlacas.clear();
        
        for (Object[] fila : datosAutobuses) {
            String id = fila[0].toString();
            String placas = fila[1].toString();
            
            idsAutobuses.add(id);
            autobusesConPlacas.put(id, placas);
        }
    }

    // Métodos auxiliares para la eliminación
    private boolean estaAutobusEnUsoEnBD(String idAutobus) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        // Suponiendo que tienes una tabla de rutas que referencia autobuses
        // Si no existe esta tabla, puedes eliminar esta verificación o adaptarla
        String query = "SELECT COUNT(*) FROM rutas WHERE id_autobus = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(idAutobus));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException | NumberFormatException ex) {
            // Si la tabla de rutas no existe, asumimos que no está en uso
            System.out.println("Tabla de rutas no encontrada, omitiendo verificación: " + ex.getMessage());
        }
        return false;
    }

    private boolean eliminarAutobusDeBD(String idAutobus) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "DELETE FROM autobuses WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(idAutobus));
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
            
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void actualizarColeccionesDesdeBD() {
        List<Object[]> datosAutobuses = obtenerAutobusesDesdeBD();
        actualizarColeccionesLocales(datosAutobuses);
    }

    private boolean existeRutaEnBD(String idRuta) {
        try {
            String query = "SELECT COUNT(*) FROM rutas WHERE id_ruta = ?";
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setString(1, idRuta);
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            rs.close();
            stmt.close();
            
            return count > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar la ruta: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }


    private boolean modificarPrecioEnBD(String idRuta, double precioBoleto) {
        try {
            String query = "UPDATE rutas SET precio_boleto = ? WHERE id_ruta = ?";
            
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setDouble(1, precioBoleto);
            stmt.setString(2, idRuta);
            
            int filasAfectadas = stmt.executeUpdate();
            stmt.close();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al modificar el precio: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }


    // Opcional: Mostrar el precio actual antes de modificarlo
    private double obtenerPrecioActual(String idRuta) {
        try {
            String query = "SELECT precio_boleto FROM rutas WHERE id_ruta = ?";
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setString(1, idRuta);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double precioActual = rs.getDouble("precio_boleto");
                rs.close();
                stmt.close();
                return precioActual;
            }
            
            rs.close();
            stmt.close();
            return 0.0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private boolean eliminarRutaEnBD(String idRuta) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "DELETE FROM rutas WHERE id_ruta = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, idRuta);
            int filasAfectadas = stmt.executeUpdate();
            
            return filasAfectadas > 0;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean existeAutobusEnBD(String idAutobus) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT COUNT(*) FROM autobuses WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(idAutobus));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    private boolean actualizarEstadoAutobusEnBD(String idAutobus, String idRuta, String estado) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        // Usamos INSERT ... ON DUPLICATE KEY UPDATE para insertar o actualizar
        String query = "INSERT INTO estado_autobuses (id_autobus, id_ruta, estado) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE id_ruta = ?, estado = ?, fecha_hora_actualizacion = CURRENT_TIMESTAMP";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, Integer.parseInt(idAutobus));
            stmt.setString(2, idRuta);
            stmt.setString(3, estado);
            stmt.setString(4, idRuta);
            stmt.setString(5, estado);
            
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Map<String, String> obtenerAutobusesDesdeBDMap() {
        Map<String, String> autobuses = new LinkedHashMap<>();
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT id, placas FROM autobuses ORDER BY id";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                autobuses.put(rs.getString("id"), rs.getString("placas"));
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return autobuses;
    }

    private Map<String, String> obtenerRutasDesdeBDMap() {
        Map<String, String> rutas = new LinkedHashMap<>();
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT id_ruta, origen, destino FROM rutas ORDER BY id_ruta";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String descripcion = rs.getString("origen") + " - " + rs.getString("destino");
                rutas.put(rs.getString("id_ruta"), descripcion);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return rutas;
    }

    private String obtenerEstadoViaje(Date horaInicio, Date horaDestino, String estadoAutobus) {
        java.util.Date ahora = new java.util.Date();
        
        // Si hay un estado específico desde la tabla estado_autobuses, lo usamos
        if (estadoAutobus != null && !estadoAutobus.isEmpty()) {
            switch (estadoAutobus) {
                case "inactivo": return "Inactivo";
                case "iniciando": return "Iniciando";
                case "en_proceso": return "En Proceso";
                case "terminado": return "Terminado";
            }
        }
        
        // Si no hay estado específico, determinamos por horario
        if (ahora.after(horaDestino)) {
            return "Finalizado";
        } else if (ahora.after(horaInicio)) {
            return "En Curso";
        } else {
            return "Pendiente";
        }
    }


    private Object[] obtenerRutaDesdeBD(String idRuta) {
        try {
            String query = "SELECT id_autobus, origen, destino, hora_inicio, hora_destino, precio_boleto " +
                        "FROM rutas WHERE id_ruta = ?";
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setString(1, idRuta);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Object[] ruta = {
                    rs.getString("id_autobus"),
                    rs.getString("origen"),
                    rs.getString("destino"),
                    rs.getTimestamp("hora_inicio"),
                    rs.getTimestamp("hora_destino"),
                    rs.getDouble("precio_boleto")  // Incluir el precio
                };
                rs.close();
                stmt.close();
                return ruta;
            }
            
            rs.close();
            stmt.close();
            return null;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener la ruta: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    private boolean existeConflictoHorarioEnBD(String idAutobus, Date horaInicio, Date horaDestino) {
        try {
            String query = "SELECT COUNT(*) FROM rutas WHERE id_autobus = ? AND " +
                        "((hora_inicio BETWEEN ? AND ?) OR (hora_destino BETWEEN ? AND ?) OR " +
                        "(? BETWEEN hora_inicio AND hora_destino) OR (? BETWEEN hora_inicio AND hora_destino))";
            
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setString(1, idAutobus);
            stmt.setTimestamp(2, new java.sql.Timestamp(horaInicio.getTime()));
            stmt.setTimestamp(3, new java.sql.Timestamp(horaDestino.getTime()));
            stmt.setTimestamp(4, new java.sql.Timestamp(horaInicio.getTime()));
            stmt.setTimestamp(5, new java.sql.Timestamp(horaDestino.getTime()));
            stmt.setTimestamp(6, new java.sql.Timestamp(horaInicio.getTime()));
            stmt.setTimestamp(7, new java.sql.Timestamp(horaDestino.getTime()));
            
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            rs.close();
            stmt.close();
            
            return count > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar conflictos de horario: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private int obtenerProximoIdViaje() {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT COALESCE(MAX(id_viaje), 0) + 1 FROM rutas";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelCentral, "Error al obtener próximo ID de viaje: " + ex.getMessage());
        }
        return 1;
    }

    private boolean registrarRutaEnBD(String idRuta, int idViaje, String idAutobus, String origen, String destino, Date horaInicio, Date horaDestino, double precioBoleto) {
        try {
            String query = "INSERT INTO rutas (id_ruta, id_viaje, id_autobus, origen, destino, hora_inicio, hora_destino, precio_boleto) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setString(1, idRuta);
            stmt.setInt(2, idViaje);
            stmt.setString(3, idAutobus);
            stmt.setString(4, origen);
            stmt.setString(5, destino);
            stmt.setTimestamp(6, new java.sql.Timestamp(horaInicio.getTime()));
            stmt.setTimestamp(7, new java.sql.Timestamp(horaDestino.getTime()));
            stmt.setDouble(8, precioBoleto);
            
            int filasAfectadas = stmt.executeUpdate();
            stmt.close();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar la ruta: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private List<Object[]> obtenerViajesDesdeBD() {
        List<Object[]> viajes = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT r.id_ruta, r.id_viaje, r.id_autobus, r.origen, r.destino, " +
                    "r.hora_inicio, r.hora_destino, " +
                    "ea.estado, ea.fecha_hora_actualizacion " +
                    "FROM rutas r " +
                    "LEFT JOIN estado_autobuses ea ON r.id_autobus = ea.id_autobus " +
                    "ORDER BY r.id_viaje";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] fila = new Object[10];
                fila[0] = rs.getString("id_ruta");
                fila[1] = rs.getInt("id_viaje");
                fila[2] = rs.getInt("id_autobus");
                fila[3] = rs.getString("origen");
                fila[4] = rs.getString("destino");
                fila[5] = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("hora_inicio"));
                fila[6] = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("hora_destino"));
                fila[7] = rs.getTimestamp("hora_inicio");
                fila[8] = rs.getTimestamp("hora_destino");
                fila[9] = rs.getString("estado");
                
                viajes.add(fila);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return viajes;
    }

    private boolean registrarRutaDuplicadaEnBD(String nuevoIdRuta, int nuevoIdViaje, Object[] rutaOriginal, Date nuevaHoraInicio, Date nuevaHoraDestino) {
        try {
            String query = "INSERT INTO rutas (id_ruta, id_viaje, id_autobus, origen, destino, " +
                        "hora_inicio, hora_destino, precio_boleto) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conexion.prepareStatement(query);
            stmt.setString(1, nuevoIdRuta);
            stmt.setInt(2, nuevoIdViaje);
            stmt.setString(3, (String) rutaOriginal[0]);  // id_autobus
            stmt.setString(4, (String) rutaOriginal[1]);  // origen
            stmt.setString(5, (String) rutaOriginal[2]);  // destino
            stmt.setTimestamp(6, new java.sql.Timestamp(nuevaHoraInicio.getTime()));
            stmt.setTimestamp(7, new java.sql.Timestamp(nuevaHoraDestino.getTime()));
            stmt.setDouble(8, (Double) rutaOriginal[5]);  // precio_boleto (clonado)
            
            int filasAfectadas = stmt.executeUpdate();
            stmt.close();
            
            return filasAfectadas > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al duplicar la ruta: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private boolean guardarConfiguracionEnBD(String tipoConfiguracion, int valor) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "INSERT INTO configuracion_capacidad (tipo_configuracion, valor) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE valor = ?, fecha_actualizacion = CURRENT_TIMESTAMP";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, tipoConfiguracion);
            stmt.setInt(2, valor);
            stmt.setInt(3, valor);
            
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelCentral, "Error al guardar configuración: " + ex.getMessage());
            return false;
        }
    }

    // Método para cargar datos de configuración desde la base de datos
    private void cargarDatosConfiguracion(DefaultTableModel tableModel) {
        String url = "jdbc:mysql://localhost:3306/sistema_autobuses";
        String user = "root";
        String password = "";
        
        String query = "SELECT tipo_configuracion, valor, fecha_actualizacion FROM configuracion_capacidad ORDER BY tipo_configuracion";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()) {
            
            // Limpiar tabla existente
            tableModel.setRowCount(0);
            
            // Llenar tabla con nuevos datos
            while (rs.next()) {
                String tipoConfig = rs.getString("tipo_configuracion");
                int valor = rs.getInt("valor");
                Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
                
                // Formatear fecha para mejor visualización
                String fechaFormateada = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaActualizacion);
                
                tableModel.addRow(new Object[]{tipoConfig, valor, fechaFormateada});
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelCentral, "Error al cargar configuración: " + ex.getMessage());
        }
    }

    // Métodos auxiliares para la conexión a la base de datos
    private boolean existeAutobusEnBD(String id, String placas) {
        return existeIdEnBD(id) || existePlacasEnBD(placas);
    }

    public SistemaAutobuses() {

        setTitle("Sistema de Autobuses");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Panel superior con botones
        panelSuperior = new JPanel();
        btnBienvenida = new JButton("Bienvenida");
        btnVentas = new JButton("Ventas");
        btnAdmin = new JButton("Administración");
        btnGerente = new JButton("Gerente");
        btnSalir = new JButton("Salir");
        
        panelSuperior.add(btnBienvenida);
        panelSuperior.add(btnVentas);
        panelSuperior.add(btnAdmin);
        panelSuperior.add(btnGerente);
        panelSuperior.add(btnSalir);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel central
        panelCentral = new JPanel(new BorderLayout());
        add(panelCentral, BorderLayout.CENTER);

        // Listeners de los botones
        btnBienvenida.addActionListener(e -> mostrarBienvenida());
        btnVentas.addActionListener(e -> mostrarVentas());
        btnAdmin.addActionListener(e -> mostrarAdministracion());  // Ahora llama al login
        btnGerente.addActionListener(e -> mostrarGerente());      // Ahora llama al login
        btnSalir.addActionListener(e -> {
            cerrarConexion();
            System.exit(0);
        });



        // Mostrar bienvenida inicial
        mostrarBienvenida();
        conectarBaseDatos();

    }

    private void cerrarConexion() {
    try {
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
            System.out.println("Conexión cerrada");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private void mostrarBienvenida() {
        panelCentral.removeAll();
        lblBienvenida = new JLabel("<html><center>¡Bienvenido al Sistema de Autobuses!<br>Seleccione una opción para continuar.</center></html>", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 32));
        lblBienvenida.setForeground(new Color(0, 102, 204));
        panelCentral.add(lblBienvenida, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    private void mostrarVentas() {
        panelCentral.removeAll();
        
        // Panel principal para ventas
        JPanel panelVentas = new JPanel(new BorderLayout());
        
        // Panel para selección de fecha
        JPanel panelFecha = new JPanel(new FlowLayout());
        JLabel lblFecha = new JLabel("Seleccionar fecha:");
        JSpinner spinnerFecha = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFecha, "yyyy-MM-dd");
        spinnerFecha.setEditor(editor);
        spinnerFecha.setValue(new Date()); // Fecha actual por defecto
        
        JButton btnAceptar = new JButton("Aceptar");
        
        panelFecha.add(lblFecha);
        panelFecha.add(spinnerFecha);
        panelFecha.add(btnAceptar);
        
        // Panel para mostrar resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        JLabel lblTituloResultados = new JLabel("Viajes programados para la fecha seleccionada", SwingConstants.CENTER);
        lblTituloResultados.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Modelo de tabla para mostrar los viajes
        DefaultTableModel modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Solo la columna del botón es editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 7) {
                    return JButton.class;
                }
                return Object.class;
            }
        };
        
        modeloTabla.addColumn("ID Viaje");
        modeloTabla.addColumn("Autobús");
        modeloTabla.addColumn("Origen");
        modeloTabla.addColumn("Destino");
        modeloTabla.addColumn("Hora Inicio");
        modeloTabla.addColumn("Hora Destino");
        modeloTabla.addColumn("Precio");
        modeloTabla.addColumn("Acción"); // Nueva columna para el botón
        
        JTable tablaViajes = new JTable(modeloTabla);
        tablaViajes.setRowHeight(30);
        
        // Agregar el renderizador y editor para el botón
        tablaViajes.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        tablaViajes.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), tablaViajes, this));
        
        JScrollPane scrollPane = new JScrollPane(tablaViajes);
        
        panelResultados.add(lblTituloResultados, BorderLayout.NORTH);
        panelResultados.add(scrollPane, BorderLayout.CENTER);
        
        // Acción del botón Aceptar
        btnAceptar.addActionListener(e -> {
            Date fechaSeleccionada = (Date) spinnerFecha.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaFormateada = sdf.format(fechaSeleccionada);
            
            // Limpiar tabla antes de cargar nuevos datos
            modeloTabla.setRowCount(0);
            
            // Consultar viajes para la fecha seleccionada
            try {
                String consulta = "SELECT r.id_viaje, a.placas, r.origen, r.destino, " +
                                "r.hora_inicio, r.hora_destino, r.precio_boleto, r.id_ruta, r.id_autobus " +
                                "FROM rutas r " +
                                "INNER JOIN autobuses a ON r.id_autobus = a.id " +
                                "WHERE DATE(r.hora_inicio) = ? OR DATE(r.hora_destino) = ? " +
                                "ORDER BY r.hora_inicio";
                
                PreparedStatement stmt = conexion.prepareStatement(consulta);
                stmt.setString(1, fechaFormateada);
                stmt.setString(2, fechaFormateada);
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Object[] fila = {
                        rs.getInt("id_viaje"),
                        rs.getString("placas"),
                        rs.getString("origen"),
                        rs.getString("destino"),
                        rs.getTimestamp("hora_inicio"),
                        rs.getTimestamp("hora_destino"),
                        rs.getDouble("precio_boleto"),
                        "Comprar Boleto", // Texto del botón
                        rs.getString("id_ruta"), // Datos adicionales para el botón
                        rs.getString("id_autobus")
                    };
                    modeloTabla.addRow(fila);
                }
                
                rs.close();
                stmt.close();
                
                if (modeloTabla.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "No hay viajes programados para la fecha seleccionada.", 
                                                "Información", JOptionPane.INFORMATION_MESSAGE);
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar los viajes: " + ex.getMessage(), 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        // Agregar componentes al panel principal
        panelVentas.add(panelFecha, BorderLayout.NORTH);
        panelVentas.add(panelResultados, BorderLayout.CENTER);
        
        panelCentral.add(panelVentas, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

        // Método para procesar la compra de boleto
    public void procesarCompraBoleto(int idViaje, String idAutobus, String idRuta, double precio) {
        // Verificar disponibilidad de asientos
        if (!verificarDisponibilidadAsientos(idAutobus, idRuta)) {
            JOptionPane.showMessageDialog(this, "No hay asientos disponibles para este viaje.", 
                                        "Sin disponibilidad", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Verificar si hay descuentos disponibles
        boolean descuentosDisponibles = verificarDescuentosDisponibles(idAutobus, idRuta);
        
        if (descuentosDisponibles) {
            // Mostrar opción para aplicar descuento
            int opcion = JOptionPane.showConfirmDialog(this, 
                "¿Desea aplicar algún descuento a su boleto?\n\n" +
                "Precio original: $" + precio,
                "Aplicar descuento", 
                JOptionPane.YES_NO_OPTION);
            
            if (opcion == JOptionPane.YES_OPTION) {
                // Mostrar tipos de descuentos disponibles
                mostrarTiposDescuentos(idViaje, idAutobus, idRuta, precio);
            } else {
                // Proceder con pago sin descuento
                solicitarMetodoPago(idViaje, idAutobus, idRuta, precio, 0, "Sin descuento");
            }
        } else {
            // No hay descuentos disponibles, proceder con pago normal
            solicitarMetodoPago(idViaje, idAutobus, idRuta, precio, 0, "Sin descuento");
        }
    }

    // Método para verificar disponibilidad de asientos
    private boolean verificarDisponibilidadAsientos(String idAutobus, String idRuta) {
        try {
            // Obtener capacidad máxima del autobús PARA ESTE VIAJE
            String queryCapacidad = "SELECT valor FROM configuracion_capacidad WHERE tipo_configuracion = 'capacidad_estabilidad'";
            PreparedStatement stmtCapacidad = conexion.prepareStatement(queryCapacidad);
            ResultSet rsCapacidad = stmtCapacidad.executeQuery();
            
            int capacidadMaxima = 0;
            if (rsCapacidad.next()) {
                capacidadMaxima = rsCapacidad.getInt("valor");
            }
            rsCapacidad.close();
            stmtCapacidad.close();
            
            // Contar boletos vendidos para ESTE VIAJE ESPECÍFICO
            String queryBoletos = "SELECT COUNT(*) as vendidos FROM boletos WHERE id_ruta = ? AND id_autobus = ?";
            PreparedStatement stmtBoletos = conexion.prepareStatement(queryBoletos);
            stmtBoletos.setString(1, idRuta);
            stmtBoletos.setString(2, idAutobus);
            ResultSet rsBoletos = stmtBoletos.executeQuery();
            
            int boletosVendidos = 0;
            if (rsBoletos.next()) {
                boletosVendidos = rsBoletos.getInt("vendidos");
            }
            rsBoletos.close();
            stmtBoletos.close();
            
            return boletosVendidos < capacidadMaxima;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar disponibilidad: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    


    // Método para verificar descuentos disponibles
    private boolean verificarDescuentosDisponibles(String idAutobus, String idRuta) {
        try {
            // Verificar si es temporada alta o baja
            String queryTemporada = "SELECT valor FROM configuracion_capacidad WHERE tipo_configuracion = 'temporada_activa'";
            PreparedStatement stmtTemporada = conexion.prepareStatement(queryTemporada);
            ResultSet rsTemporada = stmtTemporada.executeQuery();
            
            boolean esTemporadaAlta = false;
            if (rsTemporada.next()) {
                esTemporadaAlta = rsTemporada.getInt("valor") == 1;
            }
            rsTemporada.close();
            stmtTemporada.close();
            
            // Obtener capacidad máxima de descuentos según temporada
            String tipoCapacidad = esTemporadaAlta ? "capacidad_temporada_alta" : "capacidad_temporada_baja";
            String queryCapacidadDescuentos = "SELECT valor FROM configuracion_capacidad WHERE tipo_configuracion = ?";
            PreparedStatement stmtCapacidadDescuentos = conexion.prepareStatement(queryCapacidadDescuentos);
            stmtCapacidadDescuentos.setString(1, tipoCapacidad);
            ResultSet rsCapacidadDescuentos = stmtCapacidadDescuentos.executeQuery();
            
            int capacidadDescuentos = 0;
            if (rsCapacidadDescuentos.next()) {
                capacidadDescuentos = rsCapacidadDescuentos.getInt("valor");
            }
            rsCapacidadDescuentos.close();
            stmtCapacidadDescuentos.close();
            
            // Contar descuentos aplicados para ESTE VIAJE ESPECÍFICO
            String queryDescuentosAplicados = "SELECT COUNT(*) as descuentos FROM boletos WHERE id_ruta = ? AND id_autobus = ? AND tipo_descuento != 'Sin descuento'";
            PreparedStatement stmtDescuentos = conexion.prepareStatement(queryDescuentosAplicados);
            stmtDescuentos.setString(1, idRuta);
            stmtDescuentos.setString(2, idAutobus);
            ResultSet rsDescuentos = stmtDescuentos.executeQuery();
            
            int descuentosAplicados = 0;
            if (rsDescuentos.next()) {
                descuentosAplicados = rsDescuentos.getInt("descuentos");
            }
            rsDescuentos.close();
            stmtDescuentos.close();
            
            return descuentosAplicados < capacidadDescuentos;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar descuentos: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
}

    // Método para mostrar tipos de descuentos
    private void mostrarTiposDescuentos(int idViaje, String idAutobus, String idRuta, double precioOriginal) {
        try {
            // Obtener porcentajes de descuento desde la base de datos
            Map<String, Integer> descuentos = new LinkedHashMap<>();
            
            String[] tiposDescuento = {"descuento_menor", "descuento_estudiante", "descuento_profesor", "descuento_adulto_mayor"};
            String[] nombresDescuento = {"Menor de 5 años", "Estudiante", "Profesor", "Adulto mayor"};
            
            for (int i = 0; i < tiposDescuento.length; i++) {
                String query = "SELECT valor FROM configuracion_capacidad WHERE tipo_configuracion = ?";
                PreparedStatement stmt = conexion.prepareStatement(query);
                stmt.setString(1, tiposDescuento[i]);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int porcentaje = rs.getInt("valor");
                    if (porcentaje > 0) {
                        descuentos.put(nombresDescuento[i] + " (" + porcentaje + "%)", porcentaje);
                    }
                }
                rs.close();
                stmt.close();
            }
            
            if (descuentos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay descuentos disponibles en este momento.", 
                                            "Sin descuentos", JOptionPane.INFORMATION_MESSAGE);
                solicitarMetodoPago(idViaje, idAutobus, idRuta, precioOriginal, 0, "Sin descuento");
                return;
            }
            
            // Crear array de opciones para el JOptionPane
            String[] opciones = descuentos.keySet().toArray(new String[0]);
            
            // Mostrar diálogo de selección de descuento
            String descuentoSeleccionado = (String) JOptionPane.showInputDialog(this,
                "Seleccione el tipo de descuento:\nPrecio original: $" + precioOriginal,
                "Seleccionar descuento",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);
            
            if (descuentoSeleccionado != null) {
                // Extraer el porcentaje del texto seleccionado
                int porcentaje = descuentos.get(descuentoSeleccionado);
                double precioConDescuento = precioOriginal * (100 - porcentaje) / 100;
                
                // Determinar el tipo de descuento para guardar en BD
                String tipoDescuento = descuentoSeleccionado.split(" ")[0]; // Obtener la primera palabra
                
                // Mostrar confirmación
                int confirmacion = JOptionPane.showConfirmDialog(this,
                    "Descuento aplicado: " + porcentaje + "%\n" +
                    "Precio original: $" + precioOriginal + "\n" +
                    "Precio con descuento: $" + String.format("%.2f", precioConDescuento) + "\n\n" +
                    "¿Desea continuar con la compra?",
                    "Confirmar descuento",
                    JOptionPane.YES_NO_OPTION);
                
                if (confirmacion == JOptionPane.YES_OPTION) {
                    solicitarMetodoPago(idViaje, idAutobus, idRuta, precioConDescuento, porcentaje, tipoDescuento);
                }
            } else {
                // Usuario canceló, proceder sin descuento
                solicitarMetodoPago(idViaje, idAutobus, idRuta, precioOriginal, 0, "Sin descuento");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener descuentos: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Método para solicitar método de pago
    private void solicitarMetodoPago(int idViaje, String idAutobus, String idRuta, double precio, int descuento, String tipoDescuento) {
        String[] opcionesPago = {"Tarjeta", "Efectivo"};
        
        String metodoPago = (String) JOptionPane.showInputDialog(this,
            "Total a pagar: $" + String.format("%.2f", precio) + "\nSeleccione método de pago:",
            "Método de pago",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcionesPago,
            opcionesPago[0]);
        
        if (metodoPago != null) {
            if (metodoPago.equals("Tarjeta")) {
                procesarPagoTarjeta(idViaje, idAutobus, idRuta, precio, descuento, tipoDescuento);
            } else {
                procesarPagoEfectivo(idViaje, idAutobus, idRuta, precio, descuento, tipoDescuento);
            }
        }
    }

    // Método para procesar pago con tarjeta
    private void procesarPagoTarjeta(int idViaje, String idAutobus, String idRuta, double precio, int descuento, String tipoDescuento) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField txtNumeroTarjeta = new JTextField(16);
        JTextField txtTitular = new JTextField();
        JTextField txtFechaVencimiento = new JTextField(5);
        JTextField txtCVV = new JTextField(3);
        
        panel.add(new JLabel("Número de tarjeta:"));
        panel.add(txtNumeroTarjeta);
        panel.add(new JLabel("Titular:"));
        panel.add(txtTitular);
        panel.add(new JLabel("Fecha vencimiento (MM/YY):"));
        panel.add(txtFechaVencimiento);
        panel.add(new JLabel("CVV:"));
        panel.add(txtCVV);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Datos de tarjeta", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            // Validar datos básicos (implementar validaciones más robustas en producción)
            if (txtNumeroTarjeta.getText().trim().length() < 16 ||
                txtTitular.getText().trim().isEmpty() ||
                txtFechaVencimiento.getText().trim().length() != 5 ||
                txtCVV.getText().trim().length() != 3) {
                
                JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos correctamente.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Simular procesamiento de pago
            boolean pagoExitoso = simularProcesamientoTarjeta();
            
            if (pagoExitoso) {
                registrarBoleto(idViaje, idAutobus, idRuta, precio, descuento, tipoDescuento, "Tarjeta");
            } else {
                JOptionPane.showMessageDialog(this, "El pago con tarjeta fue rechazado. Intente con otro método.", 
                                            "Pago rechazado", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método para procesar pago en efectivo
    private void procesarPagoEfectivo(int idViaje, String idAutobus, String idRuta, double precio, int descuento, String tipoDescuento) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JTextField txtMontoRecibido = new JTextField();
        JLabel lblCambio = new JLabel("Cambio: $0.00");
        
        txtMontoRecibido.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { calcularCambio(); }
            @Override
            public void removeUpdate(DocumentEvent e) { calcularCambio(); }
            @Override
            public void changedUpdate(DocumentEvent e) { calcularCambio(); }
            
            private void calcularCambio() {
                try {
                    double montoRecibido = Double.parseDouble(txtMontoRecibido.getText());
                    double cambio = montoRecibido - precio;
                    if (cambio >= 0) {
                        lblCambio.setText("Cambio: $" + String.format("%.2f", cambio));
                    } else {
                        lblCambio.setText("Faltan: $" + String.format("%.2f", -cambio));
                    }
                } catch (NumberFormatException ex) {
                    lblCambio.setText("Cambio: $0.00");
                }
            }
        });
        
        panel.add(new JLabel("Total a pagar: $" + String.format("%.2f", precio)));
        panel.add(new JLabel(""));
        panel.add(new JLabel("Monto recibido:"));
        panel.add(txtMontoRecibido);
        panel.add(new JLabel(""));
        panel.add(lblCambio);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Pago en efectivo", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double montoRecibido = Double.parseDouble(txtMontoRecibido.getText());
                
                if (montoRecibido < precio) {
                    JOptionPane.showMessageDialog(this, "El monto recibido es insuficiente.", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                registrarBoleto(idViaje, idAutobus, idRuta, precio, descuento, tipoDescuento, "Efectivo");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un monto válido.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método para simular procesamiento de tarjeta (en producción, conectar con pasarela de pago real)
    private boolean simularProcesamientoTarjeta() {
        // Simular un 90% de éxito en transacciones para pruebas
        return Math.random() < 0.9;
    }

    // Método para registrar el boleto en la base de datos
    private void registrarBoleto(int idViaje, String idAutobus, String idRuta, double precio, int descuento, String tipoDescuento, String metodoPago) {
        try {
            // VERIFICACIÓN FINAL ANTES DE REGISTRAR (doble chequeo)
            if (!verificarDisponibilidadAsientos(idAutobus, idRuta)) {
                JOptionPane.showMessageDialog(this, 
                    "¡Lo sentimos! No hay asientos disponibles para este viaje.",
                    "Sin disponibilidad", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Si aplica descuento, verificar que aún haya cupo
            if (!tipoDescuento.equals("Sin descuento") && !verificarDescuentosDisponibles(idAutobus, idRuta)) {
                JOptionPane.showMessageDialog(this, 
                    "¡Lo sentimos! No hay cupo disponible para descuentos en este viaje.",
                    "Límite de descuentos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Primero necesitamos crear la tabla boletos si no existe
            String createTableQuery = "CREATE TABLE IF NOT EXISTS boletos (" +
                                    "id_boleto INT AUTO_INCREMENT PRIMARY KEY, " +
                                    "id_viaje INT, " +
                                    "id_autobus VARCHAR(10), " +
                                    "id_ruta VARCHAR(50), " +
                                    "precio DECIMAL(10,2), " +
                                    "descuento INT, " +
                                    "tipo_descuento VARCHAR(50), " +
                                    "metodo_pago VARCHAR(20), " +
                                    "fecha_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            
            PreparedStatement stmtCreate = conexion.prepareStatement(createTableQuery);
            stmtCreate.execute();
            stmtCreate.close();
            
            // Insertar el boleto
            String insertQuery = "INSERT INTO boletos (id_viaje, id_autobus, id_ruta, precio, descuento, tipo_descuento, metodo_pago) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmtInsert = conexion.prepareStatement(insertQuery);
            stmtInsert.setInt(1, idViaje);
            stmtInsert.setString(2, idAutobus);
            stmtInsert.setString(3, idRuta);
            stmtInsert.setDouble(4, precio);
            stmtInsert.setInt(5, descuento);
            stmtInsert.setString(6, tipoDescuento);
            stmtInsert.setString(7, metodoPago);
            
            int filasAfectadas = stmtInsert.executeUpdate();
            stmtInsert.close();
            
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, 
                    "¡Boleto comprado exitosamente!\n\n" +
                    "ID Viaje: " + idViaje + "\n" +
                    "Autobús: " + idAutobus + "\n" +
                    "Precio final: $" + String.format("%.2f", precio) + "\n" +
                    "Descuento: " + (descuento > 0 ? descuento + "% (" + tipoDescuento + ")" : "Ninguno") + "\n" +
                    "Método de pago: " + metodoPago,
                    "Compra exitosa", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el boleto.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar el boleto: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    

    private void abrirPanelAdmin() {
        panelCentral.removeAll();
        JPanel panelAdmin = new JPanel(new BorderLayout());
        JLabel lblAdmin = new JLabel("<html><center>¡Bienvenido al Panel de Administrador!<br>Ahora puedes gestionar el sistema.</center></html>", SwingConstants.CENTER);
        lblAdmin.setFont(new Font("Arial", Font.BOLD, 28));
        lblAdmin.setForeground(new Color(0, 153, 51));
        panelAdmin.add(lblAdmin, BorderLayout.NORTH);

        // Panel principal para contener los grupos de botones
        JPanel panelGrupos = new JPanel();
        panelGrupos.setLayout(new BoxLayout(panelGrupos, BoxLayout.Y_AXIS));
        panelGrupos.setOpaque(false);

        // --- Grupo 1: Selección de temporadas ---
        JLabel lblSeleccionTemporada = new JLabel("Seleccione temporadas:");
        lblSeleccionTemporada.setFont(new Font("Arial", Font.BOLD, 20));
        lblSeleccionTemporada.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelTemporadas = new JPanel();
        panelTemporadas.setOpaque(false);
        panelTemporadas.setLayout(new GridBagLayout());
        JButton btnTemporadaAlta = new JButton("Temporada alta, seleccione para activar");
        JButton btnTemporadaBaja = new JButton("Temporada baja, seleccione para activar");

        // --- Grupo 3: Configuración de descuentos ---
        JLabel lblConfigDescuentos = new JLabel("Configurar descuentos:");
        lblConfigDescuentos.setFont(new Font("Arial", Font.BOLD, 20));
        lblConfigDescuentos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelDescuentos = new JPanel();
        panelDescuentos.setOpaque(false);
        panelDescuentos.setLayout(new GridBagLayout());
        JButton btnDescuentoMenor = new JButton("Configurar descuento de menor de 5 años");
        JButton btnDescuentoEstudiante = new JButton("Configurar descuento de estudiante");
        JButton btnDescuentoProfesor = new JButton("Configurar descuento de profesor");
        JButton btnDescuentoAdultoMayor = new JButton("Configurar descuento de adulto mayor");

        // --- Grupo 4: Configuración de asientos y capacidades ---
        JLabel lblConfigAsientos = new JLabel("Configuración de asientos y capacidades:");
        lblConfigAsientos.setFont(new Font("Arial", Font.BOLD, 20));
        lblConfigAsientos.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelAsientos = new JPanel();
        panelAsientos.setOpaque(false);
        panelAsientos.setLayout(new GridBagLayout());
        JButton btnConfigAsientos = new JButton("Configurar máximo de asientos");
        JButton btnCapacidadEstabilidad = new JButton("Capacidad máxima del autobús quitando asientos para aumentar la estabilidad");
        JButton btnCapacidadAlta = new JButton("Establecer capacidad máxima de descuentos en temporada alta");
        JButton btnCapacidadBaja = new JButton("Establecer capacidad máxima de descuentos en temporada baja");

        // --- Grupo 5: Registro de autobuses ---
        JLabel lblRegistroAutobus = new JLabel("Registro de autobuses:");
        lblRegistroAutobus.setFont(new Font("Arial", Font.BOLD, 20));
        lblRegistroAutobus.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel panelRegistroAutobus = new JPanel();
        panelRegistroAutobus.setOpaque(false);
        panelRegistroAutobus.setLayout(new GridBagLayout());
        JButton btnRegistrarAutobus = new JButton("Registrar autobús");
        JButton btnMostrarAutobuses = new JButton("Mostrar autobuses registrados");
        JButton btnEliminarAutobus = new JButton("Eliminar autobús");










    // Acción del botón para eliminar autobús
    btnEliminarAutobus.addActionListener(e -> {
        // Primero actualizamos las colecciones locales con la base de datos
        actualizarColeccionesDesdeBD();
        
        if (autobusesConPlacas.isEmpty()) {
            JOptionPane.showMessageDialog(panelCentral, "No hay autobuses registrados para eliminar.");
            return;
        }
        
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        JComboBox<String> comboAutobuses = new JComboBox<>();
        for (String id : autobusesConPlacas.keySet()) {
            comboAutobuses.addItem(id + " - " + autobusesConPlacas.get(id));
        }
        panel.add(new JLabel("Seleccione el autobús a eliminar:"));
        panel.add(comboAutobuses);

        int result = JOptionPane.showConfirmDialog(panelCentral, panel, "Eliminar autobús", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String seleccionado = (String) comboAutobuses.getSelectedItem();
            String idAutobus = seleccionado.split(" - ")[0];
            
            // Verificar si el autobús está siendo usado en alguna ruta
            if (estaAutobusEnUsoEnBD(idAutobus)) {
                JOptionPane.showMessageDialog(panelCentral, 
                    "No se puede eliminar el autobús. Está asignado a una ruta activa.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirmacion = JOptionPane.showConfirmDialog(panelCentral, 
                "¿Está seguro de eliminar el autobús " + idAutobus + "?", 
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                // Eliminar de la base de datos
                if (eliminarAutobusDeBD(idAutobus)) {
                    // Eliminar de las colecciones locales
                    idsAutobuses.remove(idAutobus);
                    autobusesConPlacas.remove(idAutobus);
                    JOptionPane.showMessageDialog(panelCentral, "Autobús eliminado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(panelCentral, 
                        "Error al eliminar el autobús de la base de datos.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    });

        // Acción del botón para registrar autobús
        btnRegistrarAutobus.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
            JTextField txtId = new JTextField();
            JTextField txtPlacas = new JTextField();
            panel.add(new JLabel("ID del autobús:"));
            panel.add(txtId);
            panel.add(new JLabel("Placas:"));
            panel.add(txtPlacas);

            int result = JOptionPane.showConfirmDialog(panelCentral, panel, "Registrar autobús", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String id = txtId.getText().trim();
                String placas = txtPlacas.getText().trim();
                boolean repetido = false;
                StringBuilder mensaje = new StringBuilder();

                if (id.isEmpty() || placas.isEmpty()) {
                    JOptionPane.showMessageDialog(panelCentral, "Debe ingresar ambos campos.");
                    return;
                }

                // Verificar si ya existe en la base de datos
                if (existeAutobusEnBD(id, placas)) {
                    if (existeIdEnBD(id)) {
                        mensaje.append("El ID del autobús ya está registrado.\n");
                        repetido = true;
                    }
                    if (existePlacasEnBD(placas)) {
                        mensaje.append("Las placas ya están registradas.\n");
                        repetido = true;
                    }
                }

                if (repetido) {
                    JOptionPane.showMessageDialog(panelCentral, mensaje.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Registrar en la base de datos
                    if (registrarAutobusEnBD(id, placas)) {
                        idsAutobuses.add(id);
                        autobusesConPlacas.put(id, placas.toUpperCase());
                        JOptionPane.showMessageDialog(panelCentral, "Autobús registrado correctamente.");
                    } else {
                        JOptionPane.showMessageDialog(panelCentral, "Error al registrar en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Ajuste de tamaño de botones al 75% del ancho de la ventana
        int anchoVentana = (int) (getWidth() * 0.75);
        Dimension dimBoton = new Dimension(anchoVentana, 38);



        // Acción del botón para mostrar autobuses registrados
        btnMostrarAutobuses.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Autobuses Registrados", true);
            dialog.setSize(600, 300);
            dialog.setLocationRelativeTo(this);

            String[] columnas = {"ID del Autobús", "Placas"};
            
            // Obtener datos desde la base de datos
            List<Object[]> datosList = obtenerAutobusesDesdeBD();
            Object[][] datos = datosList.toArray(new Object[0][]);
            
            // También actualizamos las colecciones locales para mantener consistencia
            actualizarColeccionesLocales(datosList);

            JTable tabla = new JTable(datos, columnas);
            JScrollPane scroll = new JScrollPane(tabla);
            dialog.add(scroll, BorderLayout.CENTER);

            JButton btnCerrar = new JButton("Cerrar");
            btnCerrar.addActionListener(ev -> dialog.dispose());
            JPanel panelCerrar = new JPanel();
            panelCerrar.add(btnCerrar);
            dialog.add(panelCerrar, BorderLayout.SOUTH);

            dialog.setVisible(true);
        });

        // Acción para configurar máximo de asientos físicos
        btnConfigAsientos.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "¿Cuántos asientos físicos hay en total?", asientosFisicos > 0 ? asientosFisicos : "");
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor <= 0) throw new NumberFormatException();
                    asientosFisicos = valor;
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("asientos_fisicos", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Asientos físicos configurados: " + asientosFisicos);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido mayor a 0.");
                }
            }
        });

        // Acción para configurar capacidad máxima por estabilidad
        btnCapacidadEstabilidad.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "Capacidad máxima del autobús (quitando asientos para estabilidad):", capacidadMaximaEstabilidad > 0 ? capacidadMaximaEstabilidad : "");
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor <= 0 || (asientosFisicos > 0 && valor > asientosFisicos)) {
                        JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido mayor a 0 y no mayor a los asientos físicos.");
                        return;
                    }
                    capacidadMaximaEstabilidad = valor;
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("capacidad_estabilidad", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Capacidad máxima por estabilidad configurada: " + capacidadMaximaEstabilidad);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido mayor a 0 y no mayor a los asientos físicos.");
                }
            }
        });

        // Acción para configurar capacidad máxima de descuentos en temporada alta
        btnCapacidadAlta.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "Capacidad máxima para temporada alta:", 0);
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor < 0) {
                        JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un valor válido (mayor o igual a 0).");
                        return;
                    }
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("capacidad_temporada_alta", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Capacidad para temporada alta configurada: " + valor);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido.");
                }
            }
        });

        // Acción para configurar capacidad máxima de descuentos en temporada baja
        btnCapacidadBaja.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "Capacidad máxima para temporada baja:", 0);
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor < 0) {
                        JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un valor válido (mayor o igual a 0).");
                        return;
                    }
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("capacidad_temporada_baja", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Capacidad para temporada baja configurada: " + valor);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido.");
                }
            }
        });

        // Acciones para los botones de temporada
        btnTemporadaAlta.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(panelCentral, 
                "¿Está seguro de activar la temporada alta? Esto afectará la capacidad máxima de pasajeros.",
                "Activar Temporada Alta", 
                JOptionPane.YES_NO_OPTION);
            
            if (respuesta == JOptionPane.YES_OPTION) {
                // Aquí puedes agregar lógica adicional si necesitas marcar qué temporada está activa
                JOptionPane.showMessageDialog(panelCentral, "Temporada alta activada. La capacidad máxima será la configurada para temporada alta.");
                
                // Opcional: Podrías guardar un indicador de temporada activa en la BD
                if (guardarConfiguracionEnBD("temporada_activa", 1)) {
                    // Temporada alta activada (valor 1)
                }
            }
        });

        btnTemporadaBaja.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(panelCentral, 
                "¿Está seguro de activar la temporada baja? Esto afectará la capacidad máxima de pasajeros.",
                "Activar Temporada Baja", 
                JOptionPane.YES_NO_OPTION);
            
            if (respuesta == JOptionPane.YES_OPTION) {
                // Aquí puedes agregar lógica adicional si necesitas marcar qué temporada está activa
                JOptionPane.showMessageDialog(panelCentral, "Temporada baja activada. La capacidad máxima será la configurada para temporada baja.");
                
                // Opcional: Podrías guardar un indicador de temporada activa en la BD
                if (guardarConfiguracionEnBD("temporada_activa", 0)) {
                    // Temporada baja activada (valor 0)
                    // Podrías agregar alguna confirmación aquí si lo deseas
                }
            }
        });

        // Acciones para configurar descuentos
        btnDescuentoMenor.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "Porcentaje de descuento para menores:", 0);
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor < 0 || valor > 100) {
                        JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un porcentaje válido entre 0 y 100.");
                        return;
                    }
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("descuento_menor", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Descuento para menores configurado: " + valor + "%");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido.");
                }
            }
        });

        btnDescuentoEstudiante.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "Porcentaje de descuento para estudiantes:", 0);
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor < 0 || valor > 100) {
                        JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un porcentaje válido entre 0 y 100.");
                        return;
                    }
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("descuento_estudiante", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Descuento para estudiantes configurado: " + valor + "%");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido.");
                }
            }
        });

        btnDescuentoProfesor.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "Porcentaje de descuento para profesores:", 0);
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor < 0 || valor > 100) {
                        JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un porcentaje válido entre 0 y 100.");
                        return;
                    }
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("descuento_profesor", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Descuento para profesores configurado: " + valor + "%");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido.");
                }
            }
        });

        btnDescuentoAdultoMayor.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panelCentral, "Porcentaje de descuento para adultos mayores:", 0);
            if (input != null) {
                try {
                    int valor = Integer.parseInt(input);
                    if (valor < 0 || valor > 100) {
                        JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un porcentaje válido entre 0 y 100.");
                        return;
                    }
                    
                    // Guardar en base de datos
                    if (guardarConfiguracionEnBD("descuento_adulto_mayor", valor)) {
                        JOptionPane.showMessageDialog(panelCentral, "Descuento para adultos mayores configurado: " + valor + "%");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "Por favor, ingrese un número válido.");
                }
            }
        });

    // Ajuste de tamaño de botones
    btnTemporadaAlta.setPreferredSize(dimBoton);
    btnTemporadaAlta.setMaximumSize(dimBoton);
    btnTemporadaBaja.setPreferredSize(dimBoton);
    btnTemporadaBaja.setMaximumSize(dimBoton);
    agregarBotonGrid(panelTemporadas, btnTemporadaAlta, 0);
    agregarBotonGrid(panelTemporadas, btnTemporadaBaja, 1);

    // Grupo 3: Configurar descuentos
    btnDescuentoMenor.setPreferredSize(dimBoton);
    btnDescuentoMenor.setMaximumSize(dimBoton);
    btnDescuentoEstudiante.setPreferredSize(dimBoton);
    btnDescuentoEstudiante.setMaximumSize(dimBoton);
    btnDescuentoProfesor.setPreferredSize(dimBoton);
    btnDescuentoProfesor.setMaximumSize(dimBoton);
    btnDescuentoAdultoMayor.setPreferredSize(dimBoton);
    btnDescuentoAdultoMayor.setMaximumSize(dimBoton);
    agregarBotonGrid(panelDescuentos, btnDescuentoMenor, 0);
    agregarBotonGrid(panelDescuentos, btnDescuentoEstudiante, 1);
    agregarBotonGrid(panelDescuentos, btnDescuentoProfesor, 2);
    agregarBotonGrid(panelDescuentos, btnDescuentoAdultoMayor, 3);

    // Grupo 4: Configuración de asientos y capacidades
    btnConfigAsientos.setPreferredSize(dimBoton);
    btnConfigAsientos.setMaximumSize(dimBoton);
    btnCapacidadEstabilidad.setPreferredSize(dimBoton);
    btnCapacidadEstabilidad.setMaximumSize(dimBoton);
    btnCapacidadAlta.setPreferredSize(dimBoton);
    btnCapacidadAlta.setMaximumSize(dimBoton);
    btnCapacidadBaja.setPreferredSize(dimBoton);
    btnCapacidadBaja.setMaximumSize(dimBoton);
    agregarBotonGrid(panelAsientos, btnConfigAsientos, 0);
    agregarBotonGrid(panelAsientos, btnCapacidadEstabilidad, 1);
    agregarBotonGrid(panelAsientos, btnCapacidadAlta, 2);
    agregarBotonGrid(panelAsientos, btnCapacidadBaja, 3);

    // Grupo 5: Registro de autobuses
    btnRegistrarAutobus.setPreferredSize(dimBoton);
    btnRegistrarAutobus.setMaximumSize(dimBoton);
    btnMostrarAutobuses.setPreferredSize(dimBoton);
    btnMostrarAutobuses.setMaximumSize(dimBoton);
    btnEliminarAutobus.setPreferredSize(dimBoton);
    btnEliminarAutobus.setMaximumSize(dimBoton);
    agregarBotonGrid(panelRegistroAutobus, btnRegistrarAutobus, 0);
    agregarBotonGrid(panelRegistroAutobus, btnMostrarAutobuses, 1);
    agregarBotonGrid(panelRegistroAutobus, btnEliminarAutobus, 2);

    // Separadores visuales y textos descriptivos
    panelGrupos.add(Box.createVerticalStrut(10));
    panelGrupos.add(lblSeleccionTemporada);
    panelGrupos.add(panelTemporadas);
    panelGrupos.add(Box.createVerticalStrut(20));
    panelGrupos.add(Box.createVerticalStrut(20));
    panelGrupos.add(lblConfigDescuentos);
    panelGrupos.add(panelDescuentos);
    panelGrupos.add(Box.createVerticalStrut(20));
    panelGrupos.add(lblConfigAsientos);
    panelGrupos.add(panelAsientos);
    panelGrupos.add(Box.createVerticalStrut(20));
    panelGrupos.add(lblRegistroAutobus);
    panelGrupos.add(panelRegistroAutobus);

    // Panel para mostrar datos de configuración (derecha)
    JPanel panelDatosConfiguracion = new JPanel(new BorderLayout());
    panelDatosConfiguracion.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Color.GRAY), 
        "Configuración Actual", 
        TitledBorder.CENTER, 
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 14),
        new Color(0, 102, 204)
    ));
    panelDatosConfiguracion.setPreferredSize(new Dimension(350, 0));

    // Crear tabla para mostrar los datos
    String[] columnNames = {"Tipo Configuración", "Valor", "Última Actualización"};
    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Hacer que la tabla no sea editable
        }
    };
    
    JTable configTable = new JTable(tableModel);
    configTable.setFont(new Font("Arial", Font.PLAIN, 12));
    configTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    configTable.setRowHeight(25);
    
    JScrollPane scrollPane = new JScrollPane(configTable);
    panelDatosConfiguracion.add(scrollPane, BorderLayout.CENTER);
    
    // Botón para actualizar la tabla
    JButton btnActualizar = new JButton("Actualizar Configuración");
    btnActualizar.addActionListener(e -> {
        cargarDatosConfiguracion(tableModel);
    });
    panelDatosConfiguracion.add(btnActualizar, BorderLayout.SOUTH);

    // Cargar datos iniciales
    cargarDatosConfiguracion(tableModel);

    // Panel de contenido central con botones a la izquierda y datos a la derecha
    JPanel panelContenido = new JPanel(new BorderLayout(10, 10));
    panelContenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Panel para los botones con scroll por si hay muchos
    JScrollPane scrollBotones = new JScrollPane(panelGrupos);
    scrollBotones.setBorder(BorderFactory.createEmptyBorder());
    scrollBotones.getVerticalScrollBar().setUnitIncrement(16);
    
    panelContenido.add(scrollBotones, BorderLayout.CENTER);
    panelContenido.add(panelDatosConfiguracion, BorderLayout.EAST);

    panelAdmin.add(panelContenido, BorderLayout.CENTER);

    JButton btnCerrarSesion = new JButton("Cerrar sesión");
    btnCerrarSesion.addActionListener(e -> mostrarBienvenida());
    JPanel panelBotonCerrar = new JPanel();
    panelBotonCerrar.add(btnCerrarSesion);
    panelAdmin.add(panelBotonCerrar, BorderLayout.SOUTH);

    panelCentral.add(panelAdmin, BorderLayout.CENTER);
    panelCentral.revalidate();
    panelCentral.repaint();
}

    private void mostrarAdministracion() {
        mostrarLoginDialog("Administración", "admin");
    }



    private void abrirPanelGerente() {
        panelCentral.removeAll();
        JPanel panelGerente = new JPanel(new BorderLayout());
        
        // Título
        JLabel lblGerente = new JLabel("<html><center>¡Bienvenido al Panel de Gerente!</center></html>", SwingConstants.CENTER);
        lblGerente.setFont(new Font("Arial", Font.BOLD, 28));
        lblGerente.setForeground(new Color(153, 51, 204));
        panelGerente.add(lblGerente, BorderLayout.NORTH);

        // Panel para los botones (centro)
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 20, 20)); // 2x2 grid
        panelBotones.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

    // Botón para agregar nueva ruta
    JButton btnAgregarRuta = new JButton("Agregar Ruta");
    btnAgregarRuta.setFont(new Font("Arial", Font.BOLD, 16));
    btnAgregarRuta.setBackground(new Color(70, 130, 180));
    btnAgregarRuta.setForeground(Color.WHITE);
    btnAgregarRuta.addActionListener(e -> {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblIdRuta = new JLabel("ID de ruta:");
        JTextField txtIdRuta = new JTextField(15);
        JLabel lblIdAutobus = new JLabel("ID de autobús:");
        JTextField txtIdAutobus = new JTextField(15);
        JLabel lblOrigen = new JLabel("Origen:");
        JTextField txtOrigen = new JTextField(15);
        JLabel lblDestino = new JLabel("Destino:");
        JTextField txtDestino = new JTextField(15);
        JLabel lblHoraInicio = new JLabel("Hora de inicio:");
        JSpinner spinnerHoraInicio = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerHoraInicio, "dd/MM/yyyy HH:mm");
        spinnerHoraInicio.setEditor(editorInicio);
        JLabel lblHoraDestino = new JLabel("Hora de destino:");
        JSpinner spinnerHoraDestino = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorDestino = new JSpinner.DateEditor(spinnerHoraDestino, "dd/MM/yyyy HH:mm");
        spinnerHoraDestino.setEditor(editorDestino);
        // NUEVO CAMPO: Precio del boleto
        JLabel lblPrecioBoleto = new JLabel("Precio del boleto:");
        JTextField txtPrecioBoleto = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblIdRuta, gbc);
        gbc.gridx = 1;
        panel.add(txtIdRuta, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblIdAutobus, gbc);
        gbc.gridx = 1;
        panel.add(txtIdAutobus, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblOrigen, gbc);
        gbc.gridx = 1;
        panel.add(txtOrigen, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(lblDestino, gbc);
        gbc.gridx = 1;
        panel.add(txtDestino, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(lblHoraInicio, gbc);
        gbc.gridx = 1;
        panel.add(spinnerHoraInicio, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(lblHoraDestino, gbc);
        gbc.gridx = 1;
        panel.add(spinnerHoraDestino, gbc);

        // NUEVO CAMPO: Precio del boleto
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(lblPrecioBoleto, gbc);
        gbc.gridx = 1;
        panel.add(txtPrecioBoleto, gbc);

        int result = JOptionPane.showConfirmDialog(panelCentral, panel, "Agregar nueva ruta", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String idRuta = txtIdRuta.getText().trim();
            String idAutobus = txtIdAutobus.getText().trim();
            String origen = txtOrigen.getText().trim();
            String destino = txtDestino.getText().trim();
            java.util.Date horaInicio = (java.util.Date) spinnerHoraInicio.getValue();
            java.util.Date horaDestino = (java.util.Date) spinnerHoraDestino.getValue();
            String precioBoletoStr = txtPrecioBoleto.getText().trim();

            if (idRuta.isEmpty() || idAutobus.isEmpty() || origen.isEmpty() || destino.isEmpty() || precioBoletoStr.isEmpty()) {
                JOptionPane.showMessageDialog(panelCentral, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validar que el precio sea un número válido
            double precioBoleto;
            try {
                precioBoleto = Double.parseDouble(precioBoletoStr);
                if (precioBoleto < 0) {
                    JOptionPane.showMessageDialog(panelCentral, "El precio del boleto no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panelCentral, "El precio del boleto debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (horaInicio.after(horaDestino)) {
                JOptionPane.showMessageDialog(panelCentral, "La hora de inicio no puede ser después de la hora de destino.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validar que el idRuta no esté repetido en BD
            if (existeRutaEnBD(idRuta)) {
                JOptionPane.showMessageDialog(panelCentral, "El ID de ruta ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validar que el idAutobus exista en BD
            if (!existeAutobusEnBD(idAutobus)) {
                JOptionPane.showMessageDialog(panelCentral, "El ID de autobús no está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validar que no haya otro autobús registrado en la misma hora
            if (existeConflictoHorarioEnBD(idAutobus, horaInicio, horaDestino)) {
                JOptionPane.showMessageDialog(panelCentral, "Ya existe una ruta para este autobús en el mismo horario.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Obtener próximo ID de viaje
            int idViaje = obtenerProximoIdViaje();
            
            // Registrar en base de datos (incluyendo el precio del boleto)
            if (registrarRutaEnBD(idRuta, idViaje, idAutobus, origen, destino, horaInicio, horaDestino, precioBoleto)) {
                JOptionPane.showMessageDialog(panelCentral, "Ruta registrada correctamente. ID Viaje: " + idViaje);
            } else {
                JOptionPane.showMessageDialog(panelCentral, "Error al registrar la ruta en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });
    panelBotones.add(btnAgregarRuta);

        // Botón para marcar estado de autobús
        JButton btnEstadoAutobus = new JButton("Estado Autobús");
        btnEstadoAutobus.setFont(new Font("Arial", Font.BOLD, 16));
        btnEstadoAutobus.setBackground(new Color(60, 179, 113));
        btnEstadoAutobus.setForeground(Color.WHITE);
        btnEstadoAutobus.addActionListener(e -> {
            // Obtener autobuses y rutas disponibles desde BD
            Map<String, String> autobuses = obtenerAutobusesDesdeBDMap();
            Map<String, String> rutas = obtenerRutasDesdeBDMap();
            
            if (autobuses.isEmpty() || rutas.isEmpty()) {
                JOptionPane.showMessageDialog(panelCentral, "No hay autobuses o rutas registrados.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblAutobus = new JLabel("Seleccione autobús:");
            JComboBox<String> comboAutobuses = new JComboBox<>();
            for (String id : autobuses.keySet()) {
                comboAutobuses.addItem(id + " - " + autobuses.get(id));
            }

            JLabel lblRuta = new JLabel("Seleccione ruta:");
            JComboBox<String> comboRutas = new JComboBox<>();
            for (String id : rutas.keySet()) {
                comboRutas.addItem(id + " - " + rutas.get(id));
            }

            JLabel lblEstado = new JLabel("Estado:");
            JComboBox<String> comboEstado = new JComboBox<>(new String[]{"inactivo", "iniciando", "en_proceso", "terminado"});

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(lblAutobus, gbc);
            gbc.gridx = 1;
            panel.add(comboAutobuses, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(lblRuta, gbc);
            gbc.gridx = 1;
            panel.add(comboRutas, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(lblEstado, gbc);
            gbc.gridx = 1;
            panel.add(comboEstado, gbc);

            int result = JOptionPane.showConfirmDialog(panelCentral, panel, "Marcar Estado de Autobús", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String autobusSeleccionado = (String) comboAutobuses.getSelectedItem();
                String rutaSeleccionada = (String) comboRutas.getSelectedItem();
                String estado = (String) comboEstado.getSelectedItem();
                
                String idAutobus = autobusSeleccionado.split(" - ")[0];
                String idRuta = rutaSeleccionada.split(" - ")[0];
                
                if (actualizarEstadoAutobusEnBD(idAutobus, idRuta, estado)) {
                    JOptionPane.showMessageDialog(panelCentral, "Estado del autobús actualizado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(panelCentral, "Error al actualizar el estado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelBotones.add(btnEstadoAutobus);

        // Botón para ver viajes desde base de datos
        JButton btnVerViajesBD = new JButton("Ver Viajes BD");
        btnVerViajesBD.setFont(new Font("Arial", Font.BOLD, 16));
        btnVerViajesBD.setBackground(new Color(205, 133, 63));
        btnVerViajesBD.setForeground(Color.WHITE);
        btnVerViajesBD.addActionListener(e -> {
            // Obtener datos desde la base de datos
            List<Object[]> viajes = obtenerViajesDesdeBD();
            
            JDialog dialog = new JDialog(this, "Viajes Registrados en Base de Datos", true);
            dialog.setSize(1000, 400);
            dialog.setLocationRelativeTo(this);

            String[] columnas = {"ID Ruta", "ID Viaje", "ID Autobús", "Origen", "Destino", "Hora Inicio", "Hora Destino", "Estado Actual"};
            Object[][] datos = new Object[viajes.size()][8];

            for (int i = 0; i < viajes.size(); i++) {
                Object[] viaje = viajes.get(i);
                datos[i][0] = viaje[0]; // ID Ruta
                datos[i][1] = viaje[1]; // ID Viaje
                datos[i][2] = viaje[2]; // ID Autobús
                datos[i][3] = viaje[3]; // Origen
                datos[i][4] = viaje[4]; // Destino
                datos[i][5] = viaje[5]; // Hora Inicio (formateada)
                datos[i][6] = viaje[6]; // Hora Destino (formateada)
                datos[i][7] = obtenerEstadoViaje((java.util.Date) viaje[7], (java.util.Date) viaje[8], (String) viaje[9]); // Estado
            }

            JTable tabla = new JTable(datos, columnas);
            JScrollPane scroll = new JScrollPane(tabla);
            dialog.add(scroll, BorderLayout.CENTER);

            JButton btnCerrar = new JButton("Cerrar");
            btnCerrar.addActionListener(ev -> dialog.dispose());
            JPanel panelCerrar = new JPanel();
            panelCerrar.add(btnCerrar);
            dialog.add(panelCerrar, BorderLayout.SOUTH);

            dialog.setVisible(true);
        });
        panelBotones.add(btnVerViajesBD);

        // Botón para duplicar ruta en base de datos
        JButton btnDuplicarRuta = new JButton("Duplicar Ruta");
        btnDuplicarRuta.setFont(new Font("Arial", Font.BOLD, 16));
        btnDuplicarRuta.setBackground(new Color(186, 85, 211));
        btnDuplicarRuta.setForeground(Color.WHITE);
        btnDuplicarRuta.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            JTextField txtIdRuta = new JTextField();
            JSpinner spinnerHoraInicio = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerHoraInicio, "dd/MM/yyyy HH:mm");
            spinnerHoraInicio.setEditor(editorInicio);
            JSpinner spinnerHoraDestino = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor editorDestino = new JSpinner.DateEditor(spinnerHoraDestino, "dd/MM/yyyy HH:mm");
            spinnerHoraDestino.setEditor(editorDestino);

            panel.add(new JLabel("ID de ruta a duplicar:"));
            panel.add(txtIdRuta);
            panel.add(new JLabel("Nuevo horario de partida:"));
            panel.add(spinnerHoraInicio);
            panel.add(new JLabel("Nuevo horario de llegada:"));
            panel.add(spinnerHoraDestino);

            int result = JOptionPane.showConfirmDialog(panelCentral, panel, "Duplicar ruta", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String idRuta = txtIdRuta.getText().trim();
                java.util.Date nuevaHoraInicio = (java.util.Date) spinnerHoraInicio.getValue();
                java.util.Date nuevaHoraDestino = (java.util.Date) spinnerHoraDestino.getValue();

                if (idRuta.isEmpty()) {
                    JOptionPane.showMessageDialog(panelCentral, "Debe ingresar el ID de la ruta a duplicar.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (nuevaHoraInicio.after(nuevaHoraDestino)) {
                    JOptionPane.showMessageDialog(panelCentral, "La hora de partida no puede ser después de la hora de llegada.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Obtener la ruta original desde la base de datos
                Object[] rutaOriginal = obtenerRutaDesdeBD(idRuta);
                if (rutaOriginal == null) {
                    JOptionPane.showMessageDialog(panelCentral, "No se encontró una ruta con ese ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validar que no haya conflicto de horarios para el mismo autobús
                if (existeConflictoHorarioEnBD(String.valueOf(rutaOriginal[2]), nuevaHoraInicio, nuevaHoraDestino)) {
                    JOptionPane.showMessageDialog(panelCentral, "Ya existe una ruta para este autobús en el mismo horario.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Generar un nuevo id de ruta y obtener próximo ID de viaje
                String nuevoIdRuta = idRuta + "_dup" + System.currentTimeMillis();
                int nuevoIdViaje = obtenerProximoIdViaje();

                // Registrar la ruta duplicada en la base de datos (incluyendo el precio)
                if (registrarRutaDuplicadaEnBD(nuevoIdRuta, nuevoIdViaje, rutaOriginal, nuevaHoraInicio, nuevaHoraDestino)) {
                    JOptionPane.showMessageDialog(panelCentral, "Ruta duplicada correctamente. Nuevo ID: " + nuevoIdRuta + ", ID Viaje: " + nuevoIdViaje);
                } else {
                    JOptionPane.showMessageDialog(panelCentral, "Error al duplicar la ruta en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelBotones.add(btnDuplicarRuta);

        // Botón para mostrar autobuses registrados
        JButton btnMostrarAutobuses = new JButton("Ver Autobuses");
        btnMostrarAutobuses.setFont(new Font("Arial", Font.BOLD, 16));
        btnMostrarAutobuses.setBackground(new Color(100, 149, 237)); // Color azul medio
        btnMostrarAutobuses.setForeground(Color.WHITE);
        btnMostrarAutobuses.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Autobuses Registrados", true);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);

            String[] columnas = {"ID del Autobús", "Placas"};
            
            // Obtener datos desde la base de datos
            List<Object[]> datosList = obtenerAutobusesDesdeBD();
            Object[][] datos = datosList.toArray(new Object[0][]);
            
            // También actualizamos las colecciones locales para mantener consistencia
            actualizarColeccionesLocales(datosList);

            JTable tabla = new JTable(datos, columnas);
            tabla.setFont(new Font("Arial", Font.PLAIN, 14));
            tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
            tabla.setRowHeight(25);
            
            JScrollPane scroll = new JScrollPane(tabla);
            dialog.add(scroll, BorderLayout.CENTER);

            JButton btnCerrar = new JButton("Cerrar");
            btnCerrar.setFont(new Font("Arial", Font.BOLD, 14));
            btnCerrar.addActionListener(ev -> dialog.dispose());
            JPanel panelCerrar = new JPanel();
            panelCerrar.add(btnCerrar);
            dialog.add(panelCerrar, BorderLayout.SOUTH);

            dialog.setVisible(true);
        });
        panelBotones.add(btnMostrarAutobuses);


        // Botón para eliminar ruta
        JButton btnEliminarRuta = new JButton("Eliminar Ruta");
        btnEliminarRuta.setFont(new Font("Arial", Font.BOLD, 16));
        btnEliminarRuta.setBackground(new Color(220, 20, 60)); // Rojo para indicar eliminación
        btnEliminarRuta.setForeground(Color.WHITE);
        btnEliminarRuta.addActionListener(e -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblIdRuta = new JLabel("ID de ruta a eliminar:");
            JTextField txtIdRuta = new JTextField(15);

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(lblIdRuta, gbc);
            gbc.gridx = 1;
            panel.add(txtIdRuta, gbc);

            int result = JOptionPane.showConfirmDialog(panelCentral, panel, "Eliminar ruta", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String idRuta = txtIdRuta.getText().trim();

                if (idRuta.isEmpty()) {
                    JOptionPane.showMessageDialog(panelCentral, "El campo ID de ruta es obligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validar que la ruta exista en BD
                if (!existeRutaEnBD(idRuta)) {
                    JOptionPane.showMessageDialog(panelCentral, "El ID de ruta no existe en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Confirmación adicional por seguridad
                int confirmacion = JOptionPane.showConfirmDialog(panelCentral, 
                    "¿Está seguro de que desea eliminar la ruta con ID: " + idRuta + "?\nEsta acción no se puede deshacer.", 
                    "Confirmar eliminación", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
                if (confirmacion != JOptionPane.YES_OPTION) {
                    return;
                }
                
                // Eliminar de la base de datos
                if (eliminarRutaEnBD(idRuta)) {
                    JOptionPane.showMessageDialog(panelCentral, "Ruta eliminada correctamente.");
                } else {
                    JOptionPane.showMessageDialog(panelCentral, "Error al eliminar la ruta de la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelBotones.add(btnEliminarRuta);


        // Botón para agregar o modificar precio
        JButton btnModificarPrecio = new JButton("Agregar o Modificar Precio");
        btnModificarPrecio.setFont(new Font("Arial", Font.BOLD, 16));
        btnModificarPrecio.setBackground(new Color(70, 130, 180));
        btnModificarPrecio.setForeground(Color.WHITE);
        btnModificarPrecio.addActionListener(e -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblIdRuta = new JLabel("ID de ruta:");
            JTextField txtIdRuta = new JTextField(15);
            JLabel lblPrecioActual = new JLabel("Precio actual:");
            JLabel lblPrecioActualValor = new JLabel("N/A");
            JLabel lblPrecioBoleto = new JLabel("Nuevo precio del boleto:");
            JTextField txtPrecioBoleto = new JTextField(15);

            // Listener para actualizar el precio actual cuando se escribe el ID de ruta
            txtIdRuta.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    actualizarPrecioActual();
                }
                
                @Override
                public void removeUpdate(DocumentEvent e) {
                    actualizarPrecioActual();
                }
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                    actualizarPrecioActual();
                }
                
                private void actualizarPrecioActual() {
                    String idRuta = txtIdRuta.getText().trim();
                    if (!idRuta.isEmpty() && existeRutaEnBD(idRuta)) {
                        double precioActual = obtenerPrecioActual(idRuta);
                        lblPrecioActualValor.setText(String.format("$%.2f", precioActual));
                    } else {
                        lblPrecioActualValor.setText("N/A");
                    }
                }
            });

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(lblIdRuta, gbc);
            gbc.gridx = 1;
            panel.add(txtIdRuta, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(lblPrecioActual, gbc);
            gbc.gridx = 1;
            panel.add(lblPrecioActualValor, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(lblPrecioBoleto, gbc);
            gbc.gridx = 1;
            panel.add(txtPrecioBoleto, gbc);

            int result = JOptionPane.showConfirmDialog(panelCentral, panel, "Modificar Precio de Ruta", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String idRuta = txtIdRuta.getText().trim();
                String precioBoletoStr = txtPrecioBoleto.getText().trim();

                if (idRuta.isEmpty() || precioBoletoStr.isEmpty()) {
                    JOptionPane.showMessageDialog(panelCentral, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validar que el precio sea un número válido
                double precioBoleto;
                try {
                    precioBoleto = Double.parseDouble(precioBoletoStr);
                    if (precioBoleto < 0) {
                        JOptionPane.showMessageDialog(panelCentral, "El precio del boleto no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panelCentral, "El precio del boleto debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validar que la ruta exista en BD
                if (!existeRutaEnBD(idRuta)) {
                    JOptionPane.showMessageDialog(panelCentral, "El ID de ruta no existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Modificar precio en base de datos
                if (modificarPrecioEnBD(idRuta, precioBoleto)) {
                    JOptionPane.showMessageDialog(panelCentral, "Precio actualizado correctamente para la ruta: " + idRuta);
                } else {
                    JOptionPane.showMessageDialog(panelCentral, "Error al actualizar el precio en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panelBotones.add(btnModificarPrecio);


        panelGerente.add(panelBotones, BorderLayout.CENTER);

        // Botón cerrar sesión
        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> mostrarBienvenida());
        JPanel panelBotonCerrar = new JPanel();
        panelBotonCerrar.add(btnCerrarSesion);
        panelGerente.add(panelBotonCerrar, BorderLayout.SOUTH);

        panelCentral.add(panelGerente);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    private void mostrarGerente() {
        mostrarLoginDialog("Gerente", "gerente");
    }



    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SistemaAutobuses frame = new SistemaAutobuses();
                frame.setVisible(true);
            }
        });
    }
}
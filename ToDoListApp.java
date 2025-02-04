import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class Updatedtodo extends JFrame {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/todo";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password";

    // GUI components
    private JTextField taskField;
    private JTextField dueDateField;
    private JButton addButton;
    private JButton deleteButton; // New delete button
    private JList<String> taskList;
    private JTable taskTable;
    private DefaultTableModel tableModel;

    // Database connection object
    private Connection connection;

    public Updatedtodo() {
        // Set up the GUI
        setTitle("To-Do List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        taskField = new JTextField(20);
        dueDateField = new JTextField(10);
        addButton = new JButton("Add Task");
        deleteButton = new JButton("Delete Task"); // New delete button
        taskList = new JList<>();

        add(new JLabel("Task:"));
        add(taskField);
        add(new JLabel("Due Date:"));
        add(dueDateField);
        add(addButton);
        add(deleteButton); // Add delete button to the layout

        // Create the table model and table
        String[] columnNames = {"Task", "Due Date", "Completed"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return Boolean.class; // Set the column class to Boolean for the checkbox column
                }
                return super.getColumnClass(columnIndex);
            }
        };
        taskTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("\nConnected to database successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }

        // Add action listener to the "Add Task" button
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String task = taskField.getText();
                String dueDate = dueDateField.getText();

                // Insert the task into the database using a prepared statement
                try {
                    String query = "INSERT INTO tasks (task, due_date, completed) VALUES (?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, task);
                    preparedStatement.setString(2, dueDate);
                    preparedStatement.setBoolean(3, false); // Set the completed state to false for the new task
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                // Refresh the task list
                refreshTaskList();
            }
        });

        // Add action listener to the "Delete Task" button
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = taskTable.getSelectedRow();
                if (selectedRow != -1) {
                    String task = (String) taskTable.getValueAt(selectedRow, 0);
                    String dueDate = (String) taskTable.getValueAt(selectedRow, 1);

                    // Delete the task from the database using a prepared statement
                    try {
                        String query = "DELETE FROM tasks WHERE task = ? AND due_date = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, task);
                        preparedStatement.setString(2, dueDate);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    // Refresh the task list
                    refreshTaskList();
                }
            }
        });

        // Add action listener to the checkbox column
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = taskTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / taskTable.getRowHeight();

                if (row < taskTable.getRowCount() && row >= 0 && column < taskTable.getColumnCount() && column >= 0) {
                    Object value = taskTable.getValueAt(row, column);
                    if (value instanceof Boolean) {
                        boolean completed = (boolean) value;
                        String task = (String) taskTable.getValueAt(row, 0);
                        String dueDate = (String) taskTable.getValueAt(row, 1);

                        // Update the value in the database using a prepared statement
                        try {
                            String query = "UPDATE tasks SET completed = ? WHERE task = ? AND due_date = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(query);
                            preparedStatement.setBoolean(1, completed);
                            preparedStatement.setString(2, task);
                            preparedStatement.setString(3, dueDate);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        // Refresh the task list
                        refreshTaskList();
                    }
                }
            }
        });

        // Set up the task list
        refreshTaskList();

        // Display the GUI
        pack();
        setVisible(true);
    }

    private void refreshTaskList() {
        // Retrieve tasks from the database
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT task, due_date, completed FROM tasks";
            ResultSet resultSet = statement.executeQuery(query);

            DefaultListModel<String> model = new DefaultListModel<>();
            tableModel.setRowCount(0);

            while (resultSet.next()) {
                String task = resultSet.getString("task");
                String dueDate = resultSet.getString("due_date");
                boolean isCompleted = resultSet.getBoolean("completed");
                model.addElement(task);
                tableModel.addRow(new Object[]{task, dueDate, isCompleted});
            }

            taskList.setModel(model);

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Updatedtodo();
            }
        });
    }
}

import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            createTable(connection);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n▬▬▬▬ Estacionamento ▬▬▬▬");
                System.out.println("1. Registrar veículo");
                System.out.println("2. Atualizar veículo");
                System.out.println("3. Excluir veículo");
                System.out.println("4. Ver todos os veículos");
                System.out.println("5. Calcular tarifa");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");

                int opcao = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer do scanner

                switch (opcao) {
                    case 1 -> registrarVeiculo(scanner, connection);
                    case 2 -> atualizarVeiculo(scanner, connection);
                    case 3 -> excluirVeiculo(scanner, connection);
                    case 4 -> exibirVeiculos(connection);
                    case 5 -> calcularTarifa(scanner, connection);
                    case 0 -> {
                        System.out.println("Encerrando o programa...");
                        return;
                    }
                    default -> System.out.println("Opção inválida!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTable(Connection connection) throws SQLException {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS veiculos (" +
                "id SERIAL PRIMARY KEY," +
                "placa VARCHAR(10) NOT NULL," +
                "CPF VARCHAR(30) NOT NULL," +
                "hora_entrada TIMESTAMP NOT NULL," +
                "tarifa DECIMAL)";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableQuery);
        }
    }

    private static void registrarVeiculo(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("\n▬▬▬▬ Registrar veículo ▬▬▬▬");
        System.out.print("► Digite a placa do veículo: ");
        String placa = scanner.nextLine();

        System.out.print("► Digite o CPF do Dono do veículo: ");
        String CPF = scanner.nextLine();

        // Obter a hora atual
        Timestamp horaEntrada = new Timestamp(System.currentTimeMillis());

        String insertQuery = "INSERT INTO veiculos (placa, hora_entrada, cpf) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, placa);
            statement.setTimestamp(2, horaEntrada);
            statement.setString(3, CPF);
            statement.executeUpdate();
            System.out.println("● Veículo registrado com sucesso!");
        }
    }


    private static void atualizarVeiculo(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("\n▬▬▬▬ Atualizar veículo ▬▬▬▬");
        System.out.print("► Digite o ID do veículo que deseja atualizar: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer do scanner

        String selectQuery = "SELECT * FROM veiculos WHERE id = ?";
        String updateQuery = "UPDATE veiculos SET placa = ? WHERE id = ?";
        String updateQuery2 = "UPDATE veiculos SET CPF = ? WHERE id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement updateStatement2 = connection.prepareStatement(updateQuery2)) {
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("► Veículo encontrado:");
                System.out.println("► Placa: " + resultSet.getString("placa"));
                System.out.println("► CPF do Dono: " + resultSet.getString("CPF"));

                System.out.println("\n► Digite a nova placa do veículo:");
                System.out.print("► Nova placa: ");
                String novaPlaca = scanner.nextLine();

                System.out.println("\n► Digite o novo CPF do Dono do Veículo:");
                System.out.print("► Novo CPF: ");
                String novoCPF = scanner.nextLine();

                updateStatement.setString(1, novaPlaca);
                updateStatement.setInt(2, id);
                updateStatement.executeUpdate();

                updateStatement2.setString(1, novoCPF);
                updateStatement2.setInt(2, id);
                updateStatement2.executeUpdate();

                System.out.println("● Veículo atualizado com sucesso!");
            } else {
                System.out.println("● Veículo não encontrado!");
            }
        }
    }


    private static void excluirVeiculo(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("\n▬▬▬▬ Excluir Veículo ▬▬▬▬");
        System.out.print("► Digite o ID do veículo que deseja excluir: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer do scanner

        String deleteQuery = "DELETE FROM veiculos WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("● Veículo excluído com sucesso!");
            } else {
                System.out.println("● Veículo não encontrado!");
            }
        }
    }

    private static void exibirVeiculos(Connection connection) throws SQLException {
        System.out.println("\n▬▬▬▬ Veículos ▬▬▬▬");

        String selectQuery = "SELECT * FROM veiculos";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {
            while (resultSet.next()) {
                System.out.println("► ID: " + resultSet.getInt("id"));
                System.out.println("► Placa: " + resultSet.getString("placa"));
                System.out.println("► CPF do Dono: " + resultSet.getString("CPF"));
                System.out.println("► Horário de Entrada: " + resultSet.getString("hora_entrada"));
                System.out.println("--------------------");
            }
        }
    }

    private static void calcularTarifa(Scanner scanner, Connection connection) throws SQLException {
        System.out.println("\n▬▬▬▬ Calcular Tarifa ▬▬▬▬");
        System.out.print("► Digite o ID do veículo: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Limpa o buffer do scanner

        String selectQuery = "SELECT * FROM veiculos WHERE id = ?";
        String updateQuery = "UPDATE veiculos SET tarifa = ? WHERE id = ?";

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            selectStatement.setInt(1, id);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Veículo encontrado:");
                System.out.println("► Placa: " + resultSet.getString("placa"));
                System.out.println("► CPF do Propietário: " + resultSet.getString("CPF"));

                Timestamp horaEntrada = resultSet.getTimestamp("hora_entrada");
                Timestamp horaSaida = new Timestamp(horaEntrada.getTime() + TimeUnit.HOURS.toMillis(4));

                long tempoEstadiaMillis = horaSaida.getTime() - horaEntrada.getTime();
                long minutosEstadia = TimeUnit.MILLISECONDS.toMinutes(tempoEstadiaMillis);

                double tarifa = calcularTarifaEspecifica(minutosEstadia);

                updateStatement.setDouble(1, tarifa);
                updateStatement.setInt(2, id);
                updateStatement.executeUpdate();

                System.out.println("► A tarifa é: " + tarifa + " reais");
                //System.out.println("(Lembrando que por fins de teste é considerado 4 horas de estadia por padrão.)");
                System.out.println("● Tarifa registrada com sucesso!");
            } else {
                System.out.println("● Veículo não encontrado!");
            }
        }
    }


    private static double calcularTarifaEspecifica(long minutosEstadia) {
        // Lógica para calcular a tarifa com base no tempo de estadia em minutos
        // Exemplo: R$ 1,00 por minuto de estadia
        double tarifa = minutosEstadia * 1.0;

        return tarifa;
    }



}

package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.DTO.PaymentDtoPackage.TransactionDTO;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CSVGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generates CSV data from a list of transaction DTOs
     *
     * @param transactions The list of transaction DTOs to convert to CSV
     * @return Byte array containing the CSV data
     */
    public byte[] generateTransactionCSV(List<TransactionDTO> transactions) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            // Write CSV header
            writer.write("Transaction ID,Type,Amount,Transaction Date,Description,Customer ID,Transfer To\n");

            // Write transaction data
            for (TransactionDTO transaction : transactions) {
                writer.write(String.valueOf(transaction.getId()));
                writer.write(",");
                writer.write(escapeCsvField(transaction.getType()));
                writer.write(",");
                writer.write(String.valueOf(transaction.getAmount()));
                writer.write(",");
                writer.write(transaction.getTransactionDate() != null ?
                        transaction.getTransactionDate().format(DATE_TIME_FORMATTER) : "");
                writer.write(",");
                writer.write(escapeCsvField(transaction.getDescription()));
                writer.write(",");
                writer.write(transaction.getCustomerId() != null ?
                        transaction.getCustomerId().toString() : "");
                writer.write(",");
                writer.write(transaction.getTransferToCustomerId() != null ?
                        transaction.getTransferToCustomerId().toString() : "");
                writer.write("\n");
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV file", e);
        }
    }

    /**
     * Escapes special characters in CSV fields to prevent breaking the CSV format
     *
     * @param field The field to escape
     * @return Escaped field value
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        // If field contains quotes, commas, or newlines, wrap in quotes and escape any quotes
        if (field.contains("\"") || field.contains(",") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }
}
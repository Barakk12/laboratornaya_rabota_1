package com.example.converterfx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

class InputValidator {
    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isNumeric(String input) {
        if (isEmpty(input)) return false;
        String trimmed = input.trim();

        if (trimmed.contains("/")) {
            String[] parts = trimmed.split("/");
            if (parts.length != 2) return false;
            try {
                double num = Double.parseDouble(parts[0].trim());
                double den = Double.parseDouble(parts[1].trim());
                return den != 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (trimmed.endsWith("%")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        try {
            Double.parseDouble(trimmed);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositive(String input) {
        if (!isNumeric(input)) return false;
        return parseValue(input) > 0;
    }

    public static double parseValue(String input) {
        String trimmed = input.trim();
        if (trimmed.contains("/")) {
            String[] parts = trimmed.split("/");
            return Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim());
        }
        if (trimmed.endsWith("%")) {
            return Double.parseDouble(trimmed.substring(0, trimmed.length() - 1).trim()) / 100.0;
        }
        return Double.parseDouble(trimmed);
    }
}

class ConverterModel {
    public double convert(double value, String from, String to) {
        if (from.equals(to)) return value;

        double decimalOdds = 0;
        switch (from) {
            case "Десятичный (2.0)":
                decimalOdds = value;
                break;
            case "Проценты (50%)":
                if (value <= 0 || value > 100) throw new IllegalArgumentException("Вероятность должна быть от 0 до 100%");
                decimalOdds = 100.0 / value;
                break;
            case "Дробь (1/1)":
                if (value < 0) throw new IllegalArgumentException("Коэффициент не может быть отрицательным");
                decimalOdds = value + 1.0;
                break;
        }

        switch (to) {
            case "Десятичный (2.0)":
                return decimalOdds;
            case "Проценты (50%)":
                return 100.0 / decimalOdds;
            case "Дробь (1/1)":
                return decimalOdds - 1.0;
            default:
                return 0;
        }
    }
}

public class HelloController implements Initializable {

    @FXML private ComboBox<String> fromCombo;
    @FXML private ComboBox<String> toCombo;
    @FXML private TextField inputField;
    @FXML private Button convertButton;
    @FXML private Label resultLabel;

    private final ConverterModel model = new ConverterModel();
    private final String[] units = {"Десятичный (2.0)", "Проценты (50%)", "Дробь (1/1)"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fromCombo.setItems(FXCollections.observableArrayList(units));
        toCombo.setItems(FXCollections.observableArrayList(units));

        fromCombo.setValue(units[0]);
        toCombo.setValue(units[1]);
    }

    @FXML
    private void handleConvert() {
        String rawInput = inputField.getText();

        if (InputValidator.isEmpty(rawInput)) {
            showError("Ошибка: Поле ввода пустое!");
            return;
        }
        if (!InputValidator.isNumeric(rawInput)) {
            showError("Ошибка: Введено некорректное число или дробь!");
            return;
        }
        if (!InputValidator.isPositive(rawInput)) {
            showError("Ошибка: Значение должно быть строго больше 0!");
            return;
        }

        try {
            double value = InputValidator.parseValue(rawInput);
            String fromUnit = fromCombo.getValue();
            String toUnit = toCombo.getValue();

            double result = model.convert(value, fromUnit, toUnit);

            if (toUnit.equals("Дробь (1/1)")) {
                resultLabel.setStyle("-fx-text-fill: green;");
                resultLabel.setText(String.format("Результат: %s", convertToFraction(result)));
            } else if (toUnit.equals("Проценты (50%)")) {
                resultLabel.setStyle("-fx-text-fill: green;");
                resultLabel.setText(String.format("Результат: %.2f%%", result));
            } else {
                resultLabel.setStyle("-fx-text-fill: green;");
                resultLabel.setText(String.format("Результат: %.3f", result));
            }

        } catch (IllegalArgumentException e) {
            showError("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            showError("Произошла непредвиденная ошибка.");
        }
    }

    private void showError(String message) {
        resultLabel.setStyle("-fx-text-fill: red;");
        resultLabel.setText(message);
    }

    private String convertToFraction(double decimal) {
        if (decimal == 0) return "0";
        double tolerance = 1.0E-6;
        double h1 = 1, h2 = 0, k1 = 0, k2 = 1;
        double b = decimal;
        do {
            double a = Math.floor(b);
            double aux = h1; h1 = a * h1 + h2; h2 = aux;
            aux = k1; k1 = a * k1 + k2; k2 = aux;
            b = 1 / (b - a);
        } while (Math.abs(decimal - h1 / k1) > decimal * tolerance);

        return (long)h1 + "/" + (long)k1;
    }
}

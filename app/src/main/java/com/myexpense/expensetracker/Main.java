package com.myexpense.expensetracker;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.repository.ExpenseRepository;
import com.myexpense.expensetracker.service.ExpenseService;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        ExpenseRepository repo = new ExpenseRepository();
        ExpenseService service = new ExpenseService(repo);

        System.out.println("Total: " + service.getTotalExpenses());

        service.exportToPdf("data/expense-report.pdf");

        System.out.println("PDF generated.");
    }

}

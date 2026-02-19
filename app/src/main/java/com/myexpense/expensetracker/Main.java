package com.myexpense.expensetracker;

import com.myexpense.expensetracker.model.Expense;
import com.myexpense.expensetracker.model.Income;
import com.myexpense.expensetracker.repository.ExpenseRepository;
import com.myexpense.expensetracker.repository.IncomeRepository;
import com.myexpense.expensetracker.service.ExpenseService;
import com.myexpense.expensetracker.service.IncomeService;
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

        IncomeRepository incomeRepo = new IncomeRepository();
        IncomeService incomeService = new IncomeService(incomeRepo);

        Income inc = new Income(1000, LocalDate.now(), "Salary", "February salary");
        incomeService.addIncome(inc);

        System.out.println("Total Income: " + incomeService.getTotalIncome());

        incomeService.exportToPdf("data/income-report.pdf");
        System.out.println("Income PDF generated!");
    }

}

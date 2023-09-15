package it.apeiron.pitagora.core.theorems.dire.dto.query;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ExpensensWithMonthDTO {
    @Id
    private String _id;
    private long month;
    private Double monthExpenses;

    public ExpensensWithMonthDTO() {
    }

    public ExpensensWithMonthDTO(Integer index) {
        this._id = String.valueOf(index + 1);
    }

}

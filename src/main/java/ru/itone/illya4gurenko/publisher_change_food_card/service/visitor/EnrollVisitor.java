package ru.itone.illya4gurenko.publisher_change_food_card.service.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.Header;
import ru.itone.illya4gurenko.publisher_change_food_card.service.visitor.dto.Trailer;

@Component
@RequiredArgsConstructor
public class EnrollVisitor implements Visitor {
    private Header header;
    private Trailer trailer;
    private String lastRow;
    //заинджектить дао с методами сохранениями дао только для реп
    //если техническая ошибка то пометить файл error

    //2 переменные есть ли ошибка в трейлере или хедере

    public EnrollVisitor(String lastRow) {
        this.lastRow = lastRow;
    }

    @Override
    public void visit(Object o) {
        if (!(o instanceof String)) {
            throw new RuntimeException("no String");
        }
        String str = (String) o;
        if(header == null){
            //парсим первую строку и ее в new Header + записываем в бд
            return;
        }
        if(str.equals(lastRow)){
            //парсим подвал и ее в new Trailer + записываем в бд
            // файл закончился бросить исключение с своим эксцепшином
            return;
        }


    }

    //создать Header parseHeader(String unit) + метод валидации строки для всех типов но регулярки для ENROLL хранить здесь и для трейлера и для боди но парсить боди не надо сразу в бд
}

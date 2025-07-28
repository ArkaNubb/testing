module com.example.library {
    requires javafx.controls;
    requires javafx.fxml;


    opens Main to javafx.fxml;
    exports Main;
    exports common;
    opens common to javafx.fxml;
}

/*
*  I love Pritom and will feed him doi from dokan (matir doi)
* */
package com.jlp.wordester

class Estadisticas {
    var aciertos = 0
    var fallos = 0
    var intento1 = 0
    var intento2 = 0
    var intento3 = 0
    var intento4 = 0
    var intento5 = 0
    var intento6 = 0

    fun estadisticasToString() : String {
        var cadenaEstadisticas = aciertos.toString() + ";" + fallos.toString() + ";" + intento1 + ";" + intento2 + ";" + intento3 + ";" +intento4 + ";" +intento5 + ";" +intento6
        return cadenaEstadisticas
    }


}
// Hay 6 intentos para adivinar la palabra
// Diseñar programáticamente el layout que contiene:
// 6 Linear Horizonales dentro del Linear Base
// Dentro de cada Linear Horizontal Hay un textView por cada letra
//
package com.jlp.wordester

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.descendants

private var longitud_palabra = 5
private var letra_size = 40
private var num_intento = 1
private var num_letra = 1
private var palabra_secreta = ""
private var letra_actual = ""
private lateinit var jugada: Jugada
private var listaPalabras: ArrayList<String> = arrayListOf()
private val estadisticas : Estadisticas = Estadisticas()

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_actionbar))
        calcularScreenSize()
        nuevoJuego()
    }

    private fun nuevoJuego() {
        when (longitud_palabra) {
            5 -> {
                listaPalabras = resources.openRawResource(R.raw.palabras05).bufferedReader()
                    .use { it.readLines() } as ArrayList<String>
            }
            6 -> {
                listaPalabras = resources.openRawResource(R.raw.palabras06).bufferedReader()
                    .use { it.readLines() } as ArrayList<String>
            }
            7 -> {
                listaPalabras = resources.openRawResource(R.raw.palabras07).bufferedReader()
                    .use { it.readLines() } as ArrayList<String>
            }
            8 -> {
                listaPalabras = resources.openRawResource(R.raw.palabras08).bufferedReader()
                    .use { it.readLines() } as ArrayList<String>
            }
            9 -> {
                listaPalabras = resources.openRawResource(R.raw.palabras09).bufferedReader()
                    .use { it.readLines() } as ArrayList<String>
            }
        }

        palabra_secreta = listaPalabras.shuffled().first()
        jugada = Jugada()
        jugada.resultado.removeAll(jugada.resultado)
        jugada.palabra.removeAll(jugada.palabra)
        jugada.solucion = ""
        num_intento = 1
        num_letra = 1
        recuperarEstadisticas()
        resetarTeclado()
        dibujarPantalla()
        Log.d("Miapp", palabra_secreta)
    }


    fun clickButton(v: View) {
        when (v.tag) {
            "ENTER" -> {
                // Comprobar que el jugador haya introducido todas las letras
                if (obtenerPalabra().length < 5) {
                    Toast.makeText(
                        applicationContext,
                        "Debes introducir todas las letras de la palabra",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Comprobar que la palabra esté en el diccionario
                    if (!listaPalabras.contains(obtenerPalabra())) {
                        Toast.makeText(
                            applicationContext,
                            "La palabra tiene que estar en el diccionario",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Comparar con la solución y colorear según los aciertos
                        if (num_intento <= 6) {
                            // Comprobar aciertos
                            var colores = ""
                            var palabra_propuesta = obtenerPalabra()

                            // Mirar las letras que estén bien colocadas
                            var aciertos = 0
                            for (letra in 1..longitud_palabra) {
                                // mirar si la letra están en su posición
                                if (palabra_secreta.get(letra - 1) == palabra_propuesta.get(letra - 1)) {
                                    colores += "2"
                                    aciertos++
                                    if (aciertos == longitud_palabra) {
                                        estadisticas.aciertos++
                                        when (num_intento) {
                                            1-> estadisticas.intento1++
                                            2-> estadisticas.intento2++
                                            3-> estadisticas.intento3++
                                            4-> estadisticas.intento4++
                                            5-> estadisticas.intento5++
                                            6-> estadisticas.intento6++
                                        }
                                        var aciertotxt = ""
                                        for (n in 1..longitud_palabra) {
                                          aciertotxt += "2"
                                        }
                                        jugada.palabra.add(palabra_propuesta)
                                        jugada.resultado.add(aciertotxt)
                                        jugada.solucion = palabra_secreta
                                        guardarEstadisticas()
                                        showDialog("ACIERTO",  palabra_secreta)
                                    }
                                } else {
                                    colores += palabra_secreta.get(letra - 1)
                                }
                            }

                            // Mirar las letras que estén pero no estén bien colocadas
                            var resultado = colores
                            for (posletra in 1..longitud_palabra) {
                                // mirar si la letra están en su posición
                                if (colores.contains(palabra_propuesta.get(posletra - 1)) && colores.get(
                                        posletra - 1
                                    ) != '2'
                                ) {
                                    val poschar =
                                        colores.indexOf(palabra_propuesta.get(posletra - 1))
                                    colores =
                                        colores.substring(0, poschar) + "1" + colores.substring(
                                            poschar + 1,
                                            longitud_palabra
                                        )
                                    resultado = resultado.substring(
                                        0,
                                        posletra - 1
                                    ) + "1" + resultado.substring(posletra, longitud_palabra)
                                }
                            }
                            jugada.palabra.add(palabra_propuesta)
                            jugada.resultado.add(resultado)
                            jugada.solucion = palabra_secreta
                            colorear(resultado)
                            colorearTeclado()
                            num_intento++
                            num_letra = 1

                            if (num_intento == 7 && aciertos < longitud_palabra) {
                                // Se han completado los intentos sin resolver el puzzle
                                estadisticas.fallos++
                                guardarEstadisticas()
                                showDialog("FALLO", palabra_secreta)
                            }
                        }
                    }
                }
            }

            "BORRAR" -> {
                if (num_letra > 1) {
                    num_letra--
                    letra_actual = ""
                    findViewByTag("F" + num_intento + "L" + num_letra).text = ""
                }

            }

            else -> {
                if (num_letra <= longitud_palabra) {
                    letra_actual = v.tag.toString()
                    vertablero()
                    num_letra++
                }
            }
        }
    }

    private fun showDialog(title: String, texto: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_layout)
        val titulo = dialog.findViewById(R.id.dialogo_titulo) as TextView
        titulo.text = title
        val body = dialog.findViewById(R.id.dialogo_texto) as TextView
        body.text = "La respuesta era: " + texto + "\n\n" + verEstadisticas()
        val yesBtn = dialog.findViewById(R.id.dialogoOK) as Button
        yesBtn.setOnClickListener {
            nuevoJuego()
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun colorear(colores: String) {

        for (n in 1..longitud_palabra) {
            when (colores.substring(n - 1, n)) {
                "2" -> {
                    findViewByTag("F" + num_intento + "L" + n).setBackgroundColor(Color.GREEN)
                }
                "1" -> {
                    findViewByTag("F" + num_intento + "L" + n).setBackgroundColor(Color.YELLOW)
                }
                else -> {
                    findViewByTag("F" + num_intento + "L" + n).setBackgroundColor(Color.LTGRAY)
                }
            }
        }

    }

    private fun obtenerPalabra(): String {
        var respuesta = ""
        for (n in 1..longitud_palabra) {
            val tag_view = "F" + num_intento + "L" + n
            respuesta += findViewByTag(tag_view).text.toString()
        }
        respuesta = respuesta.lowercase()
        return respuesta
    }

    private fun vertablero() {
        val tag_actual = "F" + num_intento + "L" + num_letra
        val view_actual = findViewByTag(tag_actual)
        view_actual.text = letra_actual
    }

    private fun findViewByTag(tagActual: String): TextView {
        val linearBase = findViewById(R.id.linear_palabras) as LinearLayout
        var vista_buscada: TextView = TextView(this)
        vista_buscada.tag = "No encontrado"
        for (vista in linearBase.descendants) {
            if (vista is TextView) {
                if (vista.tag.equals(tagActual)) {
                    return vista
                }
            }
        }
        return vista_buscada
    }

    private fun findViewByTagTeclado(tagActual: String): TextView {
        val linearBase = findViewById(R.id.linear_teclado) as LinearLayout
        var vista_buscada: Button = Button(this)
        vista_buscada.tag = "No encontrado"
        for (vista in linearBase.descendants) {
            if (vista is Button) {
                if (vista.tag.equals(tagActual)) {
                    return vista
                }
            }
        }
        return vista_buscada
    }


    private fun dibujarPantalla() {
        val linear_base: LinearLayout = findViewById(R.id.linear_palabras)
        linear_base.removeAllViews()

        for (linea in 1..6) {
            val newLinea = LinearLayout(this)

            var linear_params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            linear_params.height = 0
            linear_params.weight = 1f

            newLinea.layoutParams = linear_params
            newLinea.orientation = LinearLayout.HORIZONTAL
            newLinea.id = linea


            for (letra in 1..longitud_palabra) {
                val newLetra = TextView(this)

                var params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                params.leftMargin = 10
                params.rightMargin = 10
                params.topMargin = 10
                params.bottomMargin = 10

                newLetra.tag = "F" + linea + "L" + letra
                newLetra.layoutParams = params
                newLetra.textSize = letra_size.toFloat()
                newLetra.gravity = Gravity.CENTER
                newLetra.setBackgroundColor(Color.WHITE)

                newLinea.addView(newLetra)
            }
            linear_base.addView(newLinea)
        }

    }


    private fun calcularScreenSize() {
        // Calcular tamaño letra
        val metrics = DisplayMetrics()
        val windowsManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getMetrics(metrics)
        val deviceWidth = metrics.widthPixels
        val deviceHeight = metrics.heightPixels
        letra_size = deviceWidth / (longitud_palabra * 5)
        //   Log.d("Miapp", "Ancho Pixels" + deviceWidth + " Tamaño de letra: " + letra_size)
        //   Log.d("Miapp", "Alto Pixels" + deviceHeight)
    }

    private fun guardarEstadisticas() {
        //  guardamos los resutados en shared preferences
        val PREFS_NAME = "wordester.sharedpreferences"
        val prefs: SharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, 0)
        prefs.edit().putString("ESTADISTICAS", estadisticas.estadisticasToString()).apply()
    }

    private fun recuperarEstadisticas() {
        //  Recuperar estadísticas
        val PREFS_NAME = "wordester.sharedpreferences"
        val prefs: SharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, 0)

        // Guardar las estadísticas por primera vez
        if (prefs.getString("ESTADISTICAS", "") == "") {
            guardarEstadisticas()
        }

        val cadenaEstadística = prefs.getString("ESTADISTICAS", "")
        val split = cadenaEstadística?.split(";")
        estadisticas.aciertos = split?.get(0)!!.toInt()
        estadisticas.fallos = split?.get(1)!!.toInt()
        estadisticas.intento1 = split?.get(2)!!.toInt()
        estadisticas.intento2 = split?.get(3)!!.toInt()
        estadisticas.intento3 = split?.get(4)!!.toInt()
        estadisticas.intento4 = split?.get(5)!!.toInt()
        estadisticas.intento5 = split?.get(6)!!.toInt()
        estadisticas.intento6 = split?.get(7)!!.toInt()
    }

    private fun verEstadisticas() : String {
        var jugadatxt = ""
        for (n in 0..5) {
            if (n < jugada.resultado.size) {
                val jugadatmp = jugada.resultado.get(n)
                jugadatmp.forEach { caracter ->
                    when (caracter) {
                        '1' -> jugadatxt += "🟨"
                        '2' -> jugadatxt += "🟩"
                        else -> jugadatxt +="⬜"
                    }
                }
            } else {
                for (cnd in 1..longitud_palabra) {
                    jugadatxt += "⬜"
                }
            }
            jugadatxt += "\n"
        }

        var totaljuegos = estadisticas.aciertos + estadisticas.fallos
        if (totaljuegos == 0) totaljuegos = 1
        var estatxt = jugadatxt + "\nEstadísticas: \n"  +
         "\nJugadas: " + (estadisticas.aciertos + estadisticas.fallos) +
         " -- Victorias: " + ((estadisticas.aciertos*100) / (totaljuegos)) + "%\n" +
         "Distribución:\n" +
                "1: " + ((estadisticas.intento1 * 100) / totaljuegos) + "%\n" +
                "2: " + ((estadisticas.intento2 * 100) / totaljuegos) + "%\n" +
                "3: " + ((estadisticas.intento3 * 100) / totaljuegos) + "%\n" +
                "4: " + ((estadisticas.intento4 * 100) / totaljuegos) + "%\n" +
                "5: " + ((estadisticas.intento5 * 100) / totaljuegos) + "%\n" +
                "6: " + ((estadisticas.intento6 * 100) / totaljuegos) + "%\n" +
                "X: " + ((estadisticas.fallos * 100) / totaljuegos) + "%\n\n"

                /*
                🟥🟧🟨🟩🟦🟪🟫⬛⬜
                 */

        return estatxt
    }

    private fun colorearTeclado() {
        // Colorear teclas según estén o no en la palabra secreta
        resetarTeclado()
        for (pase in 0..2) {
            for (cnd in 0..jugada.palabra.size - 1) {
                val resultadotmp = jugada.resultado.get(cnd)
                for (n in 0..longitud_palabra - 1) {
                    when (pase) {
                      0 -> {
                           if (resultadotmp.get(n) != '1' || resultadotmp.get(n) != '2') {
                               val vistaBuscada = findViewByTagTeclado(jugada.palabra.get(cnd).get(n).uppercase()) as Button
                               vistaBuscada.setBackgroundColor(Color.GRAY)
                           }
                      }
                      1 -> {
                          if (resultadotmp.get(n) == '1') {
                              val vistaBuscada = findViewByTagTeclado(jugada.palabra.get(cnd).get(n).uppercase()) as Button
                              vistaBuscada.setBackgroundColor(Color.YELLOW)
                          }
                      }
                      2 -> {
                          if (resultadotmp.get(n) == '2') {
                              val vistaBuscada = findViewByTagTeclado(jugada.palabra.get(cnd).get(n).uppercase()) as Button
                              vistaBuscada.setBackgroundColor(Color.GREEN)
                          }
                      }
                    }
                }
            }
        }

    }

    private fun resetarTeclado() {
        val linearBase = findViewById(R.id.linear_teclado) as LinearLayout
        var vista_buscada: Button = Button(this)
        vista_buscada.tag = "No encontrado"
        for (vista in linearBase.descendants) {
            vista.setBackgroundColor(resources.getColor(R.color.purple_700))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.seleccionar_num_letras -> {
                val vistaMenu = findViewById(R.id.my_actionbar) as Toolbar
                val menuLetras = PopupMenu(applicationContext, vistaMenu)
                menuLetras.inflate(R.menu.menu_num_palabras)
                menuLetras.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_letras5 -> {
                            longitud_palabra = 5
                        }
                        R.id.menu_letras6 -> {
                            longitud_palabra = 6
                        }
                        R.id.menu_letras7 -> {
                            longitud_palabra = 7
                        }
                        R.id.menu_letras8 -> {
                            longitud_palabra = 8
                        }
                        R.id.menu_letras9 -> {
                            longitud_palabra = 9
                        }

                    }
                    nuevoJuego()
                    true

                }


                menuLetras.show()


                true
            }
            R.id.menu_ayuda -> {

                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.ayuda_layout)
                val yesBtn = dialog.findViewById(R.id.button_ayuda) as Button
                yesBtn.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()


                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }


}


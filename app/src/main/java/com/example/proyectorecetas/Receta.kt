package com.example.proyectorecetas


class Receta(
    val nombre: String,
    val ingredientes: List<String>,
    val instrucciones: String,
    val tiempoPreparacion: Int,
){

    var nombreReceta: String = nombre
        get() = field
        set(value) {
            field = value
        }

    var ingredientesReceta:List<String> = ingredientes
        get() = field
        set(value){
            field=value
        }

    var instruccionesReceta:String = instrucciones
        get()= field
        set(value){
            field=value
        }


}



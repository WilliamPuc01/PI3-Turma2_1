/*
fun main() {
    //val significa VALUE(valor) e valor é imutavel
    //entendemos que val é uma constante, apesar da documentação citar
    //variavel imutavel (não está errado)
    val pi = 3.1415
	val r = 10
    
    //a função println está entre as funções built-in do kotlin
	println(pi)
    println(r)
}
*/


/*
fun main(){
    // criamos uma variavel e atribuimos a string joao
    var nome = "joão"
    
    // se tentarmos atribuir um valor de outro tipo, não funcionara
    
    nome = true
}
*/



/*
fun main(){
    //Definindo uma variavel booleana, sem explicitar o tipo boolean
    
    var sucesso = true
    
    //Definindo uma variavel inteira sem explicitar o tipo int
    
    var numero = 1
    
    // SEM INTERFERENCIA (DETERMINANDO DE UMA MANEIRA EXPLICITA O TIPO)
    
    var n: Int = 10
	var nome: String = "Joao"
    var saldo: Double = 1080.99
    
    
    
}
*/

//----------------------------------------------------------------------------------------------------------------------------------------------------------//


//pequeno programa que: calcula o comprimento da circunferencia de forma  direto
/*
fun main(){
    // constante pi (do tipo inferido Double)
    val pi = 3.1415
    val r = 10
    
    val comp = (2* pi* r)
    
    println(comp)
	
}
fun main(){
    // constante pi (do tipo inferido Double)
    val pi = 3.1415
    val r = 10
    
    (2* pi* r) // expressão sem uso porem é executado; Dizemos uma expressão morta
    
}

//funcao para calcular o comprimento da circunferencia
//devemos colocar os tipos explicitos dos parametros
*/

/*
fun calcCompCircunf(pi: Double, r: Double):Double{
    return (2 * pi * r)
}


fun main(){
    val pi = 3.1415
    val r = 10
    
   println(calcCompCircunf(pi, r)) 
}
*/
//----------------------------------------------------------------------------------------------------------------------------------------------------//
fun ehPar(numero: Int):Boolean{
    return (numero % 2 == 0)
  
}

fun main(){
    var numero = 2
    println(ehPar(numero))
}
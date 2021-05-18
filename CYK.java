//Abigail Guadarrama Victoria A01635153
import java.io.*;
import java.util.*;

public class CYK {

    public static String palabra;
    public static String simboloInicial;

    public static ArrayList<String> terminal = new ArrayList<String>();
    public static ArrayList<String> noTerminal = new ArrayList<String>();

    // Estructura que ayudará a mejorar la búsqueda. Es parecido a un diccionario
    public static TreeMap<String, ArrayList<String>> gramatica = new TreeMap<>();

    public static boolean esSimboloTerminal = false;

    //Abrir el archivo que contiene la gramática
    public static BufferedReader openFile(String archivito) {
        try {
        	File file = new File(archivito);
        	FileReader fr = new FileReader(file);
        	BufferedReader br = new BufferedReader(fr);
            return br;
        } catch (FileNotFoundException e) {
            System.out.println("Error: ¡No se encontro el archivo: " + archivito + "!");
            System.exit(1);
            return null;
        }
    }
    
    //Escanear la gramatica
    public static void procesarGramatica(String[] args) {
        BufferedReader br = openFile(args[0]);
        Scanner sc = new Scanner (br);
        ArrayList<String> tmp = new ArrayList<>();
        int line = 2;

        palabra = obtenerPalabra(args);
 
        simboloInicial = sc.next();
        sc.nextLine();

        while (sc.hasNextLine() && line <= 3) {
            tmp.addAll(Arrays.<String>asList(toArray(sc.nextLine())));
            if (line == 2) {
                terminal.addAll(tmp);
            }
            if (line == 3) {
                noTerminal.addAll(tmp);
            }
            tmp.clear();
            line++;
        }

        while (sc.hasNextLine()) {
            tmp.addAll(Arrays.<String>asList(toArray(sc.nextLine())));
            String leftSide = tmp.get(0);
            tmp.remove(0);
            gramatica.put(leftSide, new ArrayList<String>());
            gramatica.get(leftSide).addAll(tmp);
            tmp.clear();
        }
        sc.close();
        
    }
 
    //Se analiza la palabra
    public static String obtenerPalabra(String[] simnbolos) {
        if (!esSimboloTerminal) {
            return simnbolos[1];
        }
        String[] sinPalabraTerminal = new String[simnbolos.length - 1];
        for (int i = 1; i < simnbolos.length; i++) {
            sinPalabraTerminal[i - 1] = simnbolos[i];
        }
        return toString(sinPalabraTerminal);
    }

    // Se imprimen los procedimeintos
    public static void imprimirProcedimiento(String[][] tablaKYC) {
    	//Mostrar la palabra introducida
        System.out.println("Palabra: " + palabra);
        //Gramatica que se forma, incluyendo lo simbolos terminales y los generadores
        System.out.println("\nG = (" + terminal.toString().replace("[", "{").replace("]", "}") + ", "
                + noTerminal.toString().replace("[", "{").replace("]", "}") + ", P, " + simboloInicial
                + ")\n\nProducciones(Derivaciones) P:");
        
        //Se muestran las producciones correspondientes
        for (String s : gramatica.keySet()) {
            System.out.println(
                    s + " -> " + gramatica.get(s).toString().replaceAll("[\\[\\]\\,]", "").replaceAll("\\s", " | "));
        }
        System.out.println("\nResultado CYK:\n");
        dibujarTabla(tablaKYC);
    }
    
    //Dibuja la tabla correspondiente
    public static void dibujarTabla(String[][] tablaCYK) {
        int l = encontrarStringMasLargo(tablaCYK) + 2;
        String formatString = "| %-" + l + "s ";
        String s = "";
        StringBuilder sb = new StringBuilder();

        // Consturir tabla
        sb.append("+");
        for (int x = 0; x <= l + 2; x++) {
            if (x == l + 2) {
                sb.append("+");
            } else {
                sb.append("-");
            }
        }
        String barra = sb.toString();
        sb.delete(0, 1);
        String derecha = sb.toString();

        // Muestra la tabla en consola
        for (int i = 0; i < tablaCYK.length; i++) {
            for (int j = 0; j <= tablaCYK[i].length; j++) {
                System.out.print((j == 0) ? barra : (i <= 1 && j == tablaCYK[i].length - 1) ? "" : derecha);
            }
            System.out.println();
            for (int j = 0; j < tablaCYK[i].length; j++) {
                s = (tablaCYK[i][j].isEmpty()) ? "-" : tablaCYK[i][j];
                System.out.format(formatString, s.replaceAll("\\s", ","));
                if (j == tablaCYK[i].length - 1) {
                    System.out.print("|");
                }
            }
            System.out.println();
        }
        System.out.println(barra + "\n");

        // Muestra si la palabra pertenece o no a la gramatica libre de contexto
        if (tablaCYK[tablaCYK.length - 1][tablaCYK[tablaCYK.length - 1].length - 1].contains(simboloInicial)) {
            System.out.println("La palabra " + palabra + " SI pertenece a la GLC G");
        } else {
            System.out.println("La palabra " + palabra + " NO pertenece a la GLC G");
        }
    }

    public static int encontrarStringMasLargo(String[][] cykTable) {
        int x = 0;
        for (String[] s : cykTable) {
            for (String d : s) {
                if (d.length() > x) {
                    x = d.length();
                }
            }
        }
        return x;
    }

    // Se crea la tabla CYK
    public static String[][] crearTablaCYK() {
    	//Un if abreviado
        int length = esSimboloTerminal ? toArray(palabra).length : palabra.length();

        String[][] tablaCYK = new String[length + 1][];
        tablaCYK[0] = new String[length];
        for (int i = 1; i < tablaCYK.length; i++) {
            tablaCYK[i] = new String[length - (i - 1)];
        }
        for (int i = 1; i < tablaCYK.length; i++) {
            for (int j = 0; j < tablaCYK[i].length; j++) {
                tablaCYK[i][j] = "";
            }
        }
        return tablaCYK;
    }

    public static String[][] algoritmoCYK(String[][] tabla) {
        // 1.Crear la parte de arriba de la tabla
        for (int i = 0; i < tabla[0].length; i++) {
            tabla[0][i] = modificarPalabra(palabra, i);
        }

        // 2. Hacer producciones para símbolos terminales con longitud 1
        for (int i = 0; i < tabla[1].length; i++) {
            String[] validCombinations = esProductor(new String[] { tabla[0][i] });
            tabla[1][i] = toString(validCombinations);
        }
        if (palabra.length() <= 1) {
            return tabla;
        }

        // 3. Hacer producciones para símbolos con longitud 2 o más
        for (int i = 0; i < tabla[2].length; i++) {
            String[] downwards = toArray(tabla[1][i]);
            String[] diagonal = toArray(tabla[1][i + 1]);
            String[] validCombinations = esProductor(obtenerCombinaciones(downwards, diagonal));
            tabla[2][i] = toString(validCombinations);
        }
        if (palabra.length() <= 2) {
            return tabla;
        }

        // Producciones para símbolos con longitud n
        TreeSet<String> valorcitos = new TreeSet<String>();

        for (int i = 3; i < tabla.length; i++) {
            for (int j = 0; j < tabla[i].length; j++) {
                for (int compareFrom = 1; compareFrom < i; compareFrom++) {
                    String[] downwards = tabla[compareFrom][j].split("\\s");
                    String[] diagonal = tabla[i - compareFrom][j + compareFrom].split("\\s");
                    String[] combinations = obtenerCombinaciones(downwards, diagonal);
                    String[] validCombinations = esProductor(combinations);
                    if (tabla[i][j].isEmpty()) {
                        tabla[i][j] = toString(validCombinations);
                    } else {
                        String[] oldValues = toArray(tabla[i][j]);
                        ArrayList<String> newValues = new ArrayList<String>(Arrays.asList(oldValues));
                        newValues.addAll(Arrays.asList(validCombinations));
                        valorcitos.addAll(newValues);
                        tabla[i][j] = toString(valorcitos.toArray(new String[valorcitos.size()]));
                    }
                }
                valorcitos.clear();
            }
        }
        return tabla;
    }
    
    //Checa si se completó el uso de la estructura usada al principio
    public static String modificarPalabra(String word, int position) {
        if (!esSimboloTerminal) {
            return Character.toString(word.charAt(position));
        }
        return toArray(word)[position];
    }
    
    // Checa si un símbolo es productor o no
    public static String[] esProductor(String[] checar) {
        ArrayList<String> storage = new ArrayList<>();
        for (String s : gramatica.keySet()) {
            for (String current : checar) {
                if (gramatica.get(s).contains(current)) {
                    storage.add(s);
                }
            }
        }
        if (storage.size() == 0) {
            return new String[] {};
        }
        return storage.toArray(new String[storage.size()]);
    }
    
    public static String[] toArray(String input) {
        return input.split("\\s");
    }
    
    public static String[] obtenerCombinaciones(String[] from, String[] to) {
        int length = from.length * to.length;
        int counter = 0;
        String[] combinations = new String[length];
        if (length == 0) {
            return combinations;
        }
        ;
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < to.length; j++) {
                combinations[counter] = from[i] + to[j];
                counter++;
            }
        }
        return combinations;
    }

    public static String toString(String[] input) {
        return Arrays.toString(input).replaceAll("[\\[\\]\\,]", "");
    }

    public static void main(String[] args) {
    /*	S
    	a b
    	S A B E C X Y Z
    	S YB XA *
    	E YB XA
    	A a YE XC
    	B b XE YZ
    	C AA
    	X b
    	Y a
    	Z BB */
    	String [] array = {"C:/Users/Pc Digital/Documents/Universidad/Matemáticas computacionales/gramatica.txt", "abbbaba"};
    	if (array.length < 2) {
            System.out.println("Ingresar: <Archivo> <Palabra>.");
            System.exit(1);
            
        } else if (array.length > 2) {
            esSimboloTerminal = true;
            
        }
        procesarGramatica(array);
        String[][] cykTabla = crearTablaCYK();
        imprimirProcedimiento(algoritmoCYK(cykTabla));
    }

}
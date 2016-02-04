/* 
 * Copyright 2015 Alexandru Mandes.
 *
 * The code is available under a MIT License.
 *
 * Please cite: Mandes, A. (2015). Microstructure-based order placement in a 
 * continuous double auction agent based model, Algorithmic Finance 4:3-4, 
 * pp. 105-125. DOI: 10.3233/AF-150049. 
 *
 * Further reference: Cui, W. and Brabazon, A. (2012). An agent-based modeling 
 * approach to study price impact, Computational Intelligence for Financial 
 * Engineering & Economics (CIFEr), 2012 IEEE Conference on [proceedings], IEEE Press.
 */
package ccloop;

public /*final*/ class Consts {  // Singleton Pattern

    private static final Consts INSTANCE = new Consts();    // created only once with the first access/call
    
    public static int MODEL;
    
    public static int TIMETICKSPERDAY;

    public static int NULLPRICE;
    public static int PRICEDIGITS;
    public static int CASHDIGITS;

    public static boolean DEBUGMODE = false;     // print on screen console (trades)
    public static boolean DEBUGSUMMARY = false;  // print on screen console (analytics)
    public static boolean DEBUGDISK = false;    // export csv to disk (trades, quotes)

    private Consts() {
        
    }
    
    //----- decimal correction: cut-off/add digits
    
    public static int priceToMoney ( int price ) {
        
        return (int) ( price / Math.pow(10, PRICEDIGITS - CASHDIGITS) );
    }
    
    public static int moneyToPrice ( int money ) {

        return (int) ( money * Math.pow(10, PRICEDIGITS - CASHDIGITS) );
    }

}

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
package abm.CuiBrabazonMicro;

public class ParamSet {

    //--- Order palcement
    
    public double obiBase = 2;  // 2
    public int obiLevels = 3;
    public double sizePenaltyExp = 0.8; // nu = 0.7
    public double isSigmaMult = 7;    // 7
    public double dynSigmaMult = 7.5;   // 7.5
    public double alpha0 = 0.1;    // 0.1
    public double alpha1 = 0.5;     // 0.75
    public double alpha2 = 0.25;    // 0.25
    public double beta = 2.5;   // 2.5
    
    public double mu1 = 0.4;
    public double sigma1 = 0.2;
    public double mu2 = 1.1;
    public double sigma2 = 0.2;
    public double p1 = 0.4;
    
    @Override
    public String toString() { 
        
        StringBuffer buf = new StringBuffer();

        buf.append( "obiBase = " ).append( obiBase ).append("\n");
        buf.append( "obiLevels = " ).append( obiLevels ).append("\n");
        buf.append( "sizePenaltyExp = " ).append( sizePenaltyExp ).append("\n");
        buf.append( "isSigmaMult = " ).append( isSigmaMult ).append("\n");
        buf.append( "dynSigmaMult = " ).append( dynSigmaMult ).append("\n");
        buf.append( "alpha0 = " ).append( alpha0 ).append("\n");
        buf.append( "alpha1 = " ).append( alpha1 ).append("\n");
        buf.append( "alpha2 = " ).append( alpha2 ).append("\n");
        buf.append( "beta = " ).append( beta ).append("\n");
        buf.append( "mu1 = " ).append( mu1 ).append("\n");
        buf.append( "sigma1 = " ).append( sigma1 ).append("\n");
        buf.append( "mu2 = " ).append( mu2 ).append("\n");
        buf.append( "sigma2 = " ).append( sigma2 ).append("\n");
        buf.append( "p1 = " ).append( p1 ).append("\n");

        return buf.toString();
    }
}

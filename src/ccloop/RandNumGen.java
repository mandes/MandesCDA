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

import java.util.Random;

// http://www.gnu.org/software/gsl/

public class RandNumGen {

    public Random RAND;
    
    private static final double[] EXPONENTIAL_SA_QI = {0.6931471805599453, 0.9333736875190459, 
            0.9888777961838675, 0.998495925291496, 0.9998292811061389, 0.9999833164100727, 
            0.9999985691438767, 0.9999998906925558, 0.9999999924734159, 0.9999999995283275,
            0.9999999999728814, 0.9999999999985598, 0.999999999999929, 0.9999999999999968,
            0.9999999999999999, 1.0 };
    
    public RandNumGen ( long randSeed ) {
        
        this.RAND = new Random( randSeed );
    }
    
    public double nextDouble() {
        
        return RAND.nextDouble();
    }
    
    public double nextGaussian() {
        
        return RAND.nextGaussian();
    }
    
    public double nextBimodal ( double mu1, double sigma1, double mu2, double sigma2, double firstPref ) {
        
        // always positive
        
        if ( RAND.nextDouble() < firstPref ) {
            
            return Math.abs( mu1 + RAND.nextGaussian() * sigma1 );
        }
        else {
            
            return Math.abs( mu2 + RAND.nextGaussian() * sigma2 );
        }
    }
    
    public int nextPoissonian( int lambda ) {
        
        return poissonDev(lambda);
    }
    
    public double nextLaplacian ( double mu, double sigma ) {
        
        // location, scale parameters
        
        double u = RAND.nextDouble() - 0.5;

        return mu - Math.signum(u) * sigma * Math.log( 1.0 - 2.0 * Math.abs(u) );
    }

    public double nextPositiveLaplacian ( double mu, double sigma ) {

        while( true ) {
            
            double res = nextLaplacian( mu, sigma );
            
            if ( res >= 0 ) { return res; }
        }
    }

    public int nextExponential ( double beta ) {

        // f(x) = lambda * exp(-lambda * x)
        // => r = ln(1-u)/(−lambda), u in [0,1)
        // E[x] = 1/lambda = beta
        
        // f(x) = 1/beta * exp(-x/beta)
        // => r = -beta * ln(1-u)

        if ( beta <= 0 ) {
            
            throw new MyException("RandNumGen.nextExponential - /mu must be strictly positive");
        }
        
        double u;

        do {
            
            u = RAND.nextDouble();

        } while (u == 1);

        return (int) Math.round( -beta * Math.log(1 - u) );
    }
    
    public int truncExponential ( double beta, double cutoff ) {

        // 0 < x <= cutoff
        
        double u, x;

        do {
            
            do {

                u = RAND.nextDouble();
            }
            while (u == 1);

            x = -beta * Math.log(1 - u);
        }
        while ( x > cutoff );
        
        return (int) x;
    }
    
    public int nextExponential2 ( double mean ) {
        
        // Step 1:
        double a = 0;
        double u = RAND.nextDouble();

        // Step 2 and 3:
        while (u < 0.5) {
            a += EXPONENTIAL_SA_QI[0];
            u *= 2;
        }

        // Step 4 (now u >= 0.5):
        u += u - 1;

        // Step 5:
        if (u <= EXPONENTIAL_SA_QI[0]) {
            
            return (int) Math.round( mean * (a + u) );
        }

        // Step 6:
        int i = 0; // Should be 1, be we iterate before it in while using 0
        double u2 = RAND.nextDouble();
        double umin = u2;

        // Step 7 and 8:
        do {
            ++i;
            u2 = RAND.nextDouble();

            if (u2 < umin) {
                umin = u2;
            }

            // Step 8:
        } while (u > EXPONENTIAL_SA_QI[i]); // Ensured to exit since EXPONENTIAL_SA_QI[MAX] = 1

        return (int) Math.round( mean * (a + umin * EXPONENTIAL_SA_QI[0]) );
    }
    
    public int nextMixedExponential ( double w1, double mu1, double w2, double mu2 ) {
        
        // f(x) = j * exp(k*x) + m * exp(n*x)
        // f(x) = -j/k * [-k * exp(k*x)] + -m/n * [-n * exp(k*n)]
        
    /*
     * to simulate a mixed distribution you first simulate to pick which exponential the loss comes from, 
     * then you simulate the loss from the exponential.
     * 
     * 1) generate a uniform random number to use as your cumulative probability
     * 2) generate a 2nd uniform random number
     * 3) use this number to determine which of the 4 means to use and then from there just pretend you are dealing 
     * with a normal exponential and then take the inverse of this exponential's CDF to get your simulated value.
     */
        
        if ( w1 < 0 || w2 < 0 || mu1 > 0 || mu2 > 0 ) {
            
            //throw new Exception("Improper parameters!");
        }
        
        double u = RAND.nextDouble() * (-w1/mu1 - w2/mu2);
        
        if ( u < -w1/mu1 ) {
            
            return nextExponential( -mu1 );
        }
        else {
            
            return nextExponential( -mu2 );
        }
    }


    // http://tuvalu.santafe.edu/~aaronc/powerlaws/

    public double nextCuiPowerLaw ( double beta, double xmin ) {

        // exponent (slope, steepness) of the distribution: \alpha = 1 + \kappa, where \kappa is the tail index
        // x_{min} (order of magnitude)

        // power-law random number: x_{min}*(1−r)^{-1 / (1 - \beta)},
        // where r is a random number uniformly generated from (0,1)

        return xmin * Math.pow( 1.0 - RAND.nextDouble(), ( - 1.0 / (1.0 - beta) ) );
    }

    public double nextPowerLaw ( double alpha, double xmin ) {
        
        // there is a mistake in the CUI article and actually there is no minus sign in the exponent:
        // x_{min}*(1−r)^{1 / (1 - \beta)}

        return xmin * Math.pow( 1.0 - RAND.nextDouble(), ( 1.0 / (1.0 - alpha) ) );
    }

    public double nextPowerLaw ( double expon, double xmin, double xmax ) {
        
        // xmax (max market depth? 15%)
        
        // expon = 1 + tailIndex;

        return Math.pow( 
                ( Math.pow(xmax, 1.0 - expon) - Math.pow(xmin, 1.0 - expon) ) * RAND.nextDouble() + Math.pow(xmin, 1.0 - expon),
                 1.0 / (1.0 - expon) );   
    }

    public double nextLogNormal ( double scale, double shape ) {
        
        // always positive
        return Math.exp( scale + RAND.nextGaussian() * shape );
    }
        
    private int poissonDev( int lambda ) {

	double L, x;

	if ( lambda < 12.0 ) {  // direct method
        
            double p = 1.0;
            L = Math.exp(-lambda);
            x = -1;
            
            do {
                
                x++;
                p *= RAND.nextDouble();
            }
            while (p > L);
	}
        else {
            
            double y, t, sq = Math.sqrt(2.0 * lambda),
                   logLambda = Math.log(lambda);

            L = lambda * logLambda - gammaln(lambda + 1.0);

            do {
                
                do {    //y is a deviate from a Lorentzian comparison function
                    
                    y = Math.tan( Math.PI * RAND.nextDouble() );
                    x = sq * y + lambda;
                }
                while (x < 0.0);
                
                x = Math.floor(x);
                t = 0.9 * (1.0 + y * y) * Math.exp(x * logLambda - gammaln(x + 1.0) - L);
            }
            while ( RAND.nextDouble() > t );
	}
        
	return (int)x;
    }

    // natural log of the gamma function
    // when the argument z is an integer, the gamma function is just the familiar factorial function, but offset by one
    
    private double gammaln(double xx) {
        
        double y, tmp,
                ser = 1.000000000190015;
	double[] cof = {76.18009172947146,-86.50532032941677,
		24.01409824083091,-1.231739572450155,
		0.1208650973866179e-2,-0.5395239384953e-5};
	int j;

	y = xx;
        
	tmp = xx + 5.5;
	tmp -= (xx + 0.5) * Math.log(tmp);
        
	for ( j=0; j <= 5; j++ ) {
            
            y++;
            ser += cof[j] / y;
        }
        
	return -tmp + Math.log(2.5066282746310005 * ser / xx);
    }

    private int poissonRand( int lambda ) {

        //http://maths.uncommons.org/
        
        int k = 0;
        double L = Math.exp(-lambda),
            p = 1.0;

        do {
            k++;
            p *= RAND.nextDouble();
        } 
        while (p > L);

        return k-1;
    }

}

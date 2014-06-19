import org.apache.commons.math3.linear.*;

/**
 * Proof of concept, using apache commons linear algebra library to find currents in an electric circuit.
 * Solve a circuit using Kirchoff's Rules. 10 Volt battery connected to a 1 ohm resistor, that then
 * connects to two resistors (5 ohm and 2 ohm) in parallel, before returning to battery.
 */
public class EquationSolver
{
    public static void main(String[] args)
    {
        RealMatrix coefficients = new Array2DRowRealMatrix(new double[][] { {1, -1, -1},
                                                                            {-1, -5, 0},
                                                                            {-1, 0, -2} }, false);
        DecompositionSolver solver = new QRDecomposition(coefficients).getSolver();
        RealVector constants = new ArrayRealVector(new double[] {0, -10, -10}, false);
        RealVector solution = solver.solve(constants);
        
        System.out.println("I1 = " + solution.getEntry(0));
        System.out.println("I5 = " + solution.getEntry(1));
        System.out.println("I2 = " + solution.getEntry(2));
    }
}

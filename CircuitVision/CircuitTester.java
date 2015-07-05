import java.util.List;
import java.util.ArrayList;

public class CircuitTester
{
    public static void main(String[] args)
    {
        // *****************************************************
        // The following tasks will be executed in the GUI
        Circuit circuit = new Circuit(4, 4);

        circuit.addBattery(new Battery(6), 1, 0, 2, 0, 1, 0);  // Extra two arguments set the positive end of the battery.
        circuit.addComponent(new Wire(), 1, 0, 0, 0);
        circuit.addComponent(new Wire(), 0, 0, 0, 1);
        circuit.addComponent(new Wire(), 0, 1, 0, 2);
        circuit.addComponent(new Wire(), 0, 2, 1, 2);
        circuit.addComponent(new Wire(), 1, 2, 1, 1);
        
        circuit.addComponent(new Wire(), 1, 2, 1, 3);
        //circuit.addComponent(new Wire(), 1, 3, 2, 3);
        
        circuit.addComponent(new Resistor(10), 1, 1, 2, 1);
        circuit.addComponent(new Resistor(10), 1, 2, 2, 2); 
        
        circuit.addComponent(new Resistor(10), 1, 3, 2, 3); 
        //circuit.addComponent(new Resistor(10), 1, 2, 1, 3); 
        
        circuit.addComponent(new Wire(), 2, 1, 2, 2);
        circuit.addComponent(new Wire(), 2, 3, 2, 2);
        circuit.addComponent(new Wire(), 2, 2, 3, 2);
        circuit.addComponent(new Wire(), 3, 2, 3, 1);
        circuit.addComponent(new Wire(), 3, 1, 3, 0);
        circuit.addComponent(new Wire(), 3, 0, 2, 0);

        // 
        //         circuit.addComponent(new Wire(), 1, 2, 2, 2);
        //         circuit.addComponent(new Resistor(4), 2, 2, 2, 1);
        // 
        //         circuit.addComponent(new Resistor(8), 2, 2, 2, 3);   // a dead-end
        //         circuit.addComponent(new Wire(), 2, 3, 1, 3);
        //         circuit.addComponent(new Battery(4), 1, 2, 1, 3);   // defaults to making the first coordinates the pos. end
        //         circuit.addComponent(new Wire(), 2, 2, 3, 2);

        // ******************************************************

        // Calling the solve() method labels the current direction through each component, assigns a circuit branch number
        // to each component, solves for the currents, and then assigns voltages to each component and potentials to each
        // terminal.

        double[] currents = circuit.solve();

        // Display the components and terminals
        System.out.println(circuit);
        if (currents == null)
        {
            System.out.println("Error: Short Circuit or Incomplete Circuit");
        }
        else
        {
            for (int i = 0; i < currents.length; i++)
            {
                System.out.println("Current in branch " + i + " is " + currents[i]);
            }
        }
        System.out.println("\nTerminals potentials:");
        for (int r = 0; r < circuit.getRows(); r++)
        {
            for (int c = 0; c < circuit.getCols(); c++)
            {
                System.out.print(circuit.getTerminals()[r][c].getPotential() + "   ");
            }
            System.out.println();
        }

        currents = circuit.solve();

        // Display the components and terminals
        System.out.println(circuit);
        if (currents == null)
        {
            System.out.println("Error: Short Circuit or Incomplete Circuit");
        }
        else
        {
            for (int i = 0; i < currents.length; i++)
            {
                System.out.println("Current in branch " + i + " is " + currents[i]);
            }
        }
        System.out.println("\nTerminals potentials:");
        for (int r = 0; r < circuit.getRows(); r++)
        {
            for (int c = 0; c < circuit.getCols(); c++)
            {
                System.out.print(circuit.getTerminals()[r][c].getPotential() + "   ");
            }
            System.out.println();
        }
    }
}

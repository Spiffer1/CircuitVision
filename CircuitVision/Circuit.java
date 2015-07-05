import java.util.List;
import java.util.ArrayList;
import org.apache.commons.math3.linear.*;

/**
 * Holds the components and terminals for an electric circuit. The solve() method finds the current through
 * and voltage across each component of the circuit. Handles dead-end branches of a circuit correctly,
 * but may not work correctly if circuit fragments or more than one complete circuit are present.
 * Set the instance variable "verbose" to true to display results of intermediate calculations.
 */
public class Circuit
{
    private Terminal[][] terminals;
    private List<Component> components;
    private int rows;
    private int cols;
    private int numBranches;
    private boolean verbose = false;

    /**
     * Constructs a new Circuit object with a grid of terminals with particular dimensions
     * @param row The number of rows of terminals in the circuit
     * @param col The number of columns of terminals in the circuit
     */
    public Circuit(int row, int col)
    {
        rows = row;
        cols = col;
        terminals = new Terminal[rows][cols];
        components = new ArrayList<Component>();
        numBranches = 0;

        // initialize Terminals                                                
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                terminals[r][c] = new Terminal(r, c);
            }
        }
    }

    /**
     * Constructs a copy of a given circuit by adding in the same set of components. Information regarding
     * branches, currents, etc. will NOT be copied. These can be regenerated by using findNodes(),
     * labelBranches() and solve().
     * @param origCircuit The Circuit object that you wish to copy
     */
    public Circuit(Circuit origCircuit)
    {
        this(origCircuit.getRows(), origCircuit.getCols());

        for (Component comp : origCircuit.getComponents())
        {
            // get endpoint row and column for component in original circuit
            int r1 = comp.getEndPt1().getRow();
            int c1 = comp.getEndPt1().getCol();
            int r2 = comp.getEndPt2().getRow();
            int c2 = comp.getEndPt2().getCol();

            if (comp instanceof Resistor)
            {
                addComponent(new Resistor(comp.getResistance()), r1, c1, r2, c2);
            }
            else if (comp instanceof Wire)
            {
                addComponent(new Wire(), r1, c1, r2, c2);
            }
            else if (comp instanceof Battery)
            {
                Battery batt = new Battery(((Battery)comp).getVoltage());
                addBattery(batt, r1, c1, r2, c2, ((Battery)comp).getPosEnd().getRow(), ((Battery)comp).getPosEnd().getCol());
            }
        }
    }

    /**
     * This method uses several helper methods to solve a circuit via Kirchhoff's rules and linear algebra. 
     * After running it, each component will have been assigned a branch number, current, and a current direction;
     * each Terminal will have a potential.
     * @return  Returns an array of currents. Each current is indexed by its branch number within the circuit. Returns null if short circuit or no complete circuit.
     */
    public double[] solve()
    {
        // Re-initialize component values and terminal potentials
        for (Component c : components)
        {
            c.setBranch(-1);
            c.setCurrent(0);
            c.setCurrentDirection(null);
        }
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                terminals[r][c].setPotential(Double.MAX_VALUE);
            }
        }

        List<Terminal> nodes = new ArrayList<Terminal>();
        List<List<Component>> loops = new ArrayList<List<Component>>();
        int numBranches = findNodesAndLoops(nodes, loops);
        if (verbose)
        {
            System.out.println("Number of nodes: " + nodes.size());
            System.out.println("Number of loops: " + loops.size());
            System.out.println("Number of branches: " + numBranches);
        }
        if (numBranches == 0)   // No complete circuit
        {
            return null;
        }

        if (shortCircuit())
        {
            return null;
        }

        if (verbose)
        {
            System.out.println("The stored loops are:\n");
            for (int i = 0; i < loops.size(); i++)
            {
                System.out.println("Loop " + i);
                List<Component> loop = loops.get(i);
                for (Component c : loop)
                {
                    System.out.println("Loop: " + c + "  branch: " + c.getBranch());
                }
                System.out.println();
            }
        }

        // ****************************************************
        // Generate equation matrices from nodes and loops.
        double[][] coefficients;
        double[] constants;
        if (numBranches == 1)
        {
            coefficients = new double[1][1];
            constants = new double[1];
        }
        else
        {
            coefficients = new double[nodes.size() - 1 + loops.size()][numBranches];     // coefficient and constant terms
            constants = new double[nodes.size() - 1 + loops.size()];                     // for Kirchhoff's rules equations
        }

        if (verbose)
        {
            if (numBranches == 1)
            {
                System.out.println("Coefficients dimensions are: 1 x 1");
            }
            else
            {
                System.out.println("Coefficients dimensions are: " + (nodes.size() - 1 + loops.size()) + " x " + numBranches);
            }
        }
        // Get equations from nodes: Kirchoff's junction rule. Coefficients in the equations are all 1's
        // (for currents flowing into the junction) or -1's (for currents that are flowing out of the junction).
        // The constant term for each equation is 0.
        int eqnNum = 0;
        for ( ; eqnNum < nodes.size() - 1; eqnNum++)
        {
            for (Component c : nodes.get(eqnNum).getConnections())      // loops through all components connected to this node...
            {
                if (c.getCurrentDirection().equals(nodes.get(eqnNum)))  // if the current direction of this component is
                {                                                       // the same as the node, then current is flowing
                    coefficients[eqnNum][c.getBranch()]++;              // into the node
                }
                else
                {
                    coefficients[eqnNum][c.getBranch()]--;             // otherwise it's flowing out of the node
                }
            }
            constants[eqnNum] = 0;
        }
        // Get equations from loops
        int nodeEqns = eqnNum;
        for ( ; eqnNum < loops.size() + nodeEqns; eqnNum++)     // for each loop in circuit...
        {
            loops.get(eqnNum - nodeEqns).add(loops.get(eqnNum - nodeEqns).get(0)); // duplicate the first component at the end of the loop
            double voltageDrop = 0;
            for (int i = 0; i < loops.get(eqnNum - nodeEqns).size() - 1; i++)   // add voltage drops from each component in the loop
            {                                                                   // (except the last one, which is a repeat of the first)
                Component c = loops.get(eqnNum - nodeEqns).get(i);
                Component nextComponent = loops.get(eqnNum - nodeEqns).get(i + 1);
                boolean connectedToNextComponent = false;
                for (Component aConnectedComponent : c.getCurrentDirection().getConnections()) // Test whether the currentDirection
                {                                       // end of the component is connected to the next component in the loop...
                    if (nextComponent.equals(aConnectedComponent))
                    {
                        connectedToNextComponent = true;
                    }
                }
                if (connectedToNextComponent)   // If so you are walking around the loop in the component's current direction
                {                               // so you add the voltage drop.
                    coefficients[eqnNum][c.getBranch()] += c.getResistance();
                }
                else                            // Walking around loop opposite the "labeled" current direction,
                {
                    coefficients[eqnNum][c.getBranch()] -= c.getResistance();   // so it's a "negative" current and you subtract the voltage drop.
                }
            }

            // The constant term in each loop equation is due to a Voltage gain from a battery. Let's find these
            double voltageGain = 0;
            for (int i = 0; i < loops.get(eqnNum - nodeEqns).size() - 1; i++)   // go through all components in loop except last one (a repeat of the first)
            {
                Component c = loops.get(eqnNum - nodeEqns).get(i);
                if (c instanceof Battery)
                {
                    Component nextComponent = loops.get(eqnNum - nodeEqns).get(i + 1);
                    boolean connectedToNextComponent = false;
                    // Determine whether the direction you are walking through the loop is from the neg. to pos. terminal of the battery
                    for (Component aConnectedComponent : ((Battery)c).getPosEnd().getConnections())
                    {
                        if (nextComponent.equals(aConnectedComponent))   // if the posive end is connected to the next component in the loop...
                        {
                            connectedToNextComponent = true;
                        }
                    }
                    if (connectedToNextComponent)
                    {
                        voltageGain += ((Battery)c).getVoltage();            // then it's a voltage gain. (Walking through battery from neg to pos.)
                    }
                    else
                    {
                        voltageGain -= ((Battery)c).getVoltage();   // Otherwise you are walking around the loop from pos to neg through the battery.
                    }
                }
            }
            constants[eqnNum] = voltageGain;
            loops.get(eqnNum - nodeEqns).remove(loops.get(eqnNum - nodeEqns).size() - 1); // Remove the last component from the loop (the duplicated of first component)
        }

        if (verbose)
        {
            // Print coefficient Matrix and Constant vector
            System.out.println("Coefficients:\n");
            for (int r = 0; r < coefficients.length; r++)
            {
                for (int c = 0; c < coefficients[r].length; c++)
                {
                    System.out.print(coefficients[r][c] + "  ");
                }
                System.out.println("\n");
            }
            System.out.println("Constants:\n");
            for (int i = 0; i < constants.length; i++)
            {
                System.out.print(constants[i] + "  ");
            }
            System.out.println("\n");
        }

        // *************************************************************
        //Send equations to EquationSolver (Apache Commons Linear Algebra package)
        RealMatrix coefs = new Array2DRowRealMatrix(coefficients, false);
        DecompositionSolver solver = new QRDecomposition(coefs).getSolver();
        RealVector consts = new ArrayRealVector(constants, false);
        RealVector solution = null;
        try
        {
            solution = solver.solve(consts);
        }
        catch (SingularMatrixException e)   // Likely a short circuit
        {
            return null;
        }
        //Extract the currents from the EquationSolver's solution vector...
        double[] currents = new double[solution.getDimension()];    // holds the current in each branch
        for (int i = 0; i < solution.getDimension(); i++)
        {
            currents[i] = solution.getEntry(i);
        }
        //***************************************************************

        // Update component currents
        for (Component c : components)
        {
            if (c.getBranch() < 999)
            {
                c.setCurrent(currents[c.getBranch()]);
            }
            else
            {
                c.setCurrent(0);
            }
        }
        if (verbose)
        {
            System.out.println(this);
        }
        calculatePotentials(loops, nodes);
        if (verbose)
        {
            System.out.println("Terminal potentials:");
            for (int r = 0; r < rows; r++)
            {
                for (int c  = 0; c < cols; c++)
                {
                    System.out.print(terminals[r][c].getPotential() + "\t");
                }
                System.out.println();
            }
            System.out.println();
        }
        return currents;
    }

    /**
     * Identifies if a short circuit exists: complete loop with no resistors and at least one battery.
     * @return True if short circuit exists; false otherwise.
     */
    private boolean shortCircuit()
    {
        boolean shortCirc = false;
        Circuit copy = new Circuit(this);
        for (int i = copy.getComponents().size() - 1; i >= 0; i--)
        {
            Component c = copy.getComponents().get(i);
            {
                if (c instanceof Resistor)
                {
                    copy.removeComponent(c);
                }
            }
        }        
        List<Terminal> nodes = new ArrayList<Terminal>();
        List<List<Component>> loops = new ArrayList<List<Component>>();
        int copyBranches = copy.findNodesAndLoops(nodes, loops);
        if (copyBranches != 0)   // At least one loop found
        {
            for (List<Component> loop : loops)
            {
                for (Component c : loop)
                {
                    if (c instanceof Battery)
                    {
                        shortCirc = true;
                    }
                }
            }
        }
        return shortCirc;
    }

    /**
     * This method uses helper methods to: 
     *     (1) populate the ArrayList of nodes (junctions) for a circuit;
     *     (2) assign a current/branch number to each independent branch of the circuit; dead-ends are branch 999
     *     (3) assign a current direction to each component within each branch;
     *     (4) populate a List of loops, wherein each loop is a List of the components in that loop;
     * @param nodes  An empty ArrayList that will be populated with the terminals that are junctions in the circuit.
     * @param origloops  An empty ArrayList of Lists of components that will be populated with components from circuit loops.
     * @return The number of branches in the circuit, excluding any dead-end branches.
     */
    private int findNodesAndLoops(List<Terminal> nodes, List<List<Component>> origLoops)
    {
        // Original circuit, including any dead-ends
        findNodes(nodes);
        labelBranches(nodes);
        if (numBranches == 0)   // There is not a complete circuit
        {
            return 0;
        }
        // A copy of the circuit that will have dead-ends removed.
        Circuit equationCopy = new Circuit(this);
        equationCopy.findNodes(nodes);
        equationCopy.labelBranches(nodes);
        Component deadEnd = equationCopy.getComponents().get(0); // just to be not null
        while (deadEnd != null)    // Remove one dangling component at a time, until there are no more
        {
            deadEnd = equationCopy.removeDangler();
            if (deadEnd != null)
            {
                Component orig = equationCopy.findCorrespondingComponent(this, deadEnd);
                orig.setBranch(999);
            }
        }
        
        equationCopy.findNodes(nodes);  // The nodes list is now properly updated for writing circuit equations.
        equationCopy.labelBranches(nodes);

        // Update original circuit with branch numbers and currentDirections from the copy that has had deadends trimmed off.
        for (Component c : equationCopy.getComponents())
        {
            Component orig = findCorrespondingComponent(this, c);
            orig.setBranch(c.getBranch());     
            orig.setCurrentDirection(c.getCurrentDirection());
        }

        // Make a copy of circuit and nodes that can be modified during the loop analysis. As loops are identified,
        // components will be removed from the copy circuit, so that different loops can be found.
        Circuit copy = new Circuit(equationCopy);
        List<Terminal> copyNodes = new ArrayList<Terminal>();
        copy.findNodes(copyNodes);
        copy.labelBranches(copyNodes);

        numBranches = copy.getNumBranches();

        // Makes a List parallel to origLoops. Each is an arrayList of arraylists of components within each loop.
        List<List<Component>> copyLoops = new ArrayList<List<Component>>(); // loops in the copy circuit, not the original circuit 
        int loopCounter = 0;
        while (copy.getComponents().size() > 0)
        {
            copyLoops.add(new ArrayList<Component>());
            origLoops.add(new ArrayList<Component>());
            //  make array of terminals in loop
            List<Terminal> copyTerms = new ArrayList<Terminal>();
            Component comp = copy.getComponents().get(0);
            Terminal prevTerm = comp.getEndPt1();
            copyTerms.add(prevTerm);
            boolean endLoop = false;
            while (!endLoop)
            {
                //walk around loop, adding terminals and components to their arrays 
                copyLoops.get(loopCounter).add(comp);
                origLoops.get(loopCounter).add(findCorrespondingComponent(this, comp));  // corresponding component in orig circuit
                Terminal nextTerm = comp.getEndPt2();
                if (nextTerm == prevTerm)   // then nextTerm is not actually the next terminal in the loop...
                {
                    nextTerm = comp.getEndPt1();    // so get the other end of the component
                }

                if (copyTerms.contains(nextTerm))   // you have reached a terminal you have seen before
                {
                    endLoop = true;
                }
                else
                {   // get one of the connected components (that is not the component you just added to the loop)
                    Component nextComp = nextTerm.getConnections().get(0);
                    if (comp == nextComp)
                    {
                        nextComp = nextTerm.getConnections().get(1);
                    }
                    prevTerm = nextTerm;
                    comp = nextComp;
                }
                copyTerms.add(nextTerm);
            }
            // Once you find a terminal that is already in the loop, there may still be a dangling end at the start of the loop.
            // That is, it may be shaped like a "9". So trim off the initial components until the start and end terminals are the same.
            while (copyTerms.get(0) != copyTerms.get(copyTerms.size() - 1)) 
            {
                //trim terminals and components off start of lists
                copyTerms.remove(0);
                copyLoops.get(loopCounter).remove(0);
                origLoops.get(loopCounter).remove(0);
            }
            // To find the next independent loop, remove a component (and dangling ends) from the loop you just found.
            // That way you won't find the exact same loop the next time.
            copy.removeComponent(copyLoops.get(loopCounter).get(0));
            Component dangler = copy.getComponents().get(0); // just to be not null;
            while (dangler != null)    // Remove one dangling component at a time, until there are no more
            {
                dangler = copy.removeDangler();
            }
            loopCounter++;
        }
        return copy.getNumBranches();
    }

    /**
     * Given a component (givenComp) in one circuit, this method finds the component attached to the same terminals
     * in a different circuit (circ). If there is not a component attached to the same terminals, this returns null.
     * @param circ  The circuit you are searching in
     * @param givenComp  The component from the original circuit
     * @return  The component that is found connected to the same terminals as comp, but in a different circuit, circ, 
     *          or null if no such component exists in circ
     */
    private Component findCorrespondingComponent(Circuit circ, Component givenComp)
    {
        for (Component comp : circ.getComponents())
        {
            if ( (comp.getEndPt1().equals(givenComp.getEndPt1()) && comp.getEndPt2().equals(givenComp.getEndPt2())) || (comp.getEndPt1().equals(givenComp.getEndPt2()) && comp.getEndPt2().equals(givenComp.getEndPt1())) )
            {
                return comp;
            }
        }
        return null;
    }

    /**
     * If a circuit has dead-ends, this method removes the last component in a dead-end branch.
     * @return Returns the component that was removed if there was one; null otherwise.
     */
    private Component removeDangler()
    {
        for (int i = components.size() - 1; i >= 0; i--)    // Loop through all circuit components from right end of list
        {                                                   // to do a "for-loop removal" and not skip any components
            Component component = components.get(i);

            // If either end of component is connected to only one component (i.e. itself), then it's a dangler
            List<Component> endPt1Connections = component.getEndPt1().getConnections();
            List<Component> endPt2Connections = component.getEndPt2().getConnections();
            if (endPt1Connections.size() == 1 || endPt2Connections.size() == 1)
            {
                Component componentCopy = new Wire();
                componentCopy.setEndPt1(component.getEndPt1());
                componentCopy.setEndPt2(component.getEndPt2());
                removeComponent(component);
                return componentCopy;
            }
        }
        return null;
    }

    /**
     * Searches the 2-D array of terminals for a circuit to find junctions. Adds any terminals that have three or more connections
     * to a List of nodes. The provided list of nodes is first cleared, and then repopulated.
     * @param nodes  A reference to a List of terminals.
     */
    private void findNodes(List<Terminal> nodes)
    {
        nodes.clear();
        for (int r = 0; r < getRows(); r++)
        {
            for (int c = 0; c < getCols(); c++)
            {
                if (getTerminals()[r][c].numConnections() > 2)
                {
                    nodes.add(getTerminals()[r][c]);
                }
            }
        }
    }

    /**
     * Labels each component in the circuit with a branch number. The current through all components in a branch are the same,
     * so branch numbers correspond to current variables in the circuit equations. E.g. current[0] = current through components
     * labeled with branch 0. This method also assigns a current direction to each component within a branch. Branches that are 
     * dead-ends are given the branch number 999. (May not identify all deadends.)
     * @param nodes  The List of nodes (junctions) in the circuit.
     * @return False if it finds no complete circuit; true otherwise.
     */
    private boolean labelBranches(List<Terminal> nodes)
    {
        numBranches = 0;
        if (components.size() == 0)
        {
            return false;
        }
        if (nodes.size() == 0)  // Circuit is a single loop without junctions (or a single incomplete complete)
        {
            Component c = components.get(0);
            Terminal prevTerm = c.getEndPt1();
            for (int i = 0; i < components.size(); i++)
            {
                c.setBranch(0);     // only one loop, so all components are branch 0
                Terminal nextTerminal = c.getEndPt2();
                if (nextTerminal == prevTerm)   // make sure you get the terminal at the opposite end of the component
                {
                    nextTerminal = c.getEndPt1();
                }
                if (nextTerminal.numConnections() < 2)  // a dead end in the circuit
                {
                    return false;
                }
                c.setCurrentDirection(nextTerminal);
                Component nextComponent = nextTerminal.getConnection(0);
                if (nextComponent == c)
                {
                    nextComponent = nextTerminal.getConnection(1);
                }
                c = nextComponent;
                prevTerm = nextTerminal;
            }
            numBranches = 1;    // the complete loop is one branch: branch #0
        }
        else    // there are multiple branches
        {
            // Reset branch number of each component to -1, the default for unassigned branches
            for (Component c : components)
            {
                c.setBranch(-1);
            }
            // loop through every connection of every node...
            for (Terminal node : nodes)
            {
                for (int i = 0; i < node.numConnections(); i++)     // for each connection to that node...
                {
                    Component c = node.getConnection(i);
                    if (c.getBranch() < 0)  // has not yet been assigned a current
                    {
                        boolean endBranch = false;
                        List<Component> branchComponents = new ArrayList<Component>();
                        Terminal t = node;
                        while (!endBranch)
                        {
                            branchComponents.add(c);
                            c.setBranch(numBranches);
                            // get other end of component and set equal to nextTerminal
                            Terminal nextTerminal = c.getEndPt1();
                            if (nextTerminal == t)
                            {
                                nextTerminal = c.getEndPt2();
                            }
                            if (nextTerminal.numConnections() < 2)  // a dead end in the circuit...
                            {
                                endBranch = true;
                                for (Component deadEnd : branchComponents)  // set dead-end branch components to branch 999
                                {
                                    deadEnd.setBranch(999);
                                }
                                numBranches--;    // so that branch number can get reused
                                break;
                            }
                            c.setCurrentDirection(nextTerminal);
                            if (nextTerminal.numConnections() > 2)  // have reached another junction
                            {
                                endBranch = true;
                            }
                            else    // move on to the next terminal and next component within the branch
                            {
                                t = nextTerminal;
                                Component nextComponent = t.getConnection(0);
                                if (nextComponent == c)
                                {
                                    nextComponent = t.getConnection(1);
                                }
                                c = nextComponent;
                            }
                        }
                        numBranches++;
                    }
                }
            }
        }
        // Any component that is not attached to the main circuit gets assigned branch #999
        for (Component c : components)
        {
            if (c.getBranch() == -1)
            {
                c.setBranch(999);
            }
        }
        return true;
    }

    /**
     * Finds the potential at each terminal in the circuit. Disconnected terminals are left at their default potental
     * of Double.MAX_VALUE.
     * @param loops  A List of the ArrayLists of components in each circuit loop
     * @param nodes  A List of the circuit nodes/junctions
     */
    private void calculatePotentials(List<List<Component>> loops, List<Terminal> nodes)
    {
        // Find a terminal in a loop; assign it potential 0
        Component comp = loops.get(0).get(0);
        comp.getEndPt1().setPotential(0);

        // Make a copy of the circuit components. Loop through all copied components finding those that have a potential set at only one end.
        // Then calculate and set potential for the other end and remove that component from the copy List.
        List<Component> componentsCopy = new ArrayList<Component>();
        for (Component c : components)
        {
            componentsCopy.add(c);
        }

        boolean updateOccurred = true;
        while (updateOccurred)
        {
            updateOccurred = false;
            for (int i  = 0; i < componentsCopy.size(); i++)
            {
                Component c = componentsCopy.get(i);
                double potential1 = c.getEndPt1().getPotential();
                double potential2 = c.getEndPt2().getPotential(); 
                if (potential1 < Double.MAX_VALUE / 10 && potential2 >= Double.MAX_VALUE / 10 || potential2 < Double.MAX_VALUE / 10 && potential1 >= Double.MAX_VALUE / 10)
                {
                    // Other end's potential is known end's potential + component's voltage gain
                    Terminal knownEnd = c.getEndPt1();
                    Terminal otherEnd = c.getEndPt2();
                    if (knownEnd.getPotential() >= Double.MAX_VALUE / 10)
                    {
                        knownEnd = c.getEndPt2();
                        otherEnd = c.getEndPt1();
                    }

                    if (c instanceof Battery)
                    {
                        if (knownEnd.equals( ((Battery)c).getPosEnd() ))
                        {
                            otherEnd.setPotential(knownEnd.getPotential() - ((Battery)c).getVoltage());
                        }
                        else
                        {
                            otherEnd.setPotential(knownEnd.getPotential() + ((Battery)c).getVoltage());
                        }
                    }
                    else
                    {
                        if (c.getBranch() < 999)
                        {
                            if (c.getCurrentDirection() != null && c.getCurrentDirection().equals(knownEnd))    
                            {
                                otherEnd.setPotential(knownEnd.getPotential() + c.getResistance() * c.getCurrent());
                            }
                            else
                            {
                                otherEnd.setPotential(knownEnd.getPotential() - c.getResistance() * c.getCurrent());
                            }
                        }
                        else
                        {
                            otherEnd.setPotential(knownEnd.getPotential());
                        }
                    }
                    updateOccurred = true;
                    componentsCopy.remove(i);
                    break;
                }
            }
        }

        // Add or subtract to all the potentials so that the minimum potential is 0 Volts
        double minVolts = terminals[0][0].getPotential();
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (terminals[r][c].getPotential() < minVolts)
                {
                    minVolts = terminals[r][c].getPotential();
                }
            }
        }
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                if (terminals[r][c].getPotential() != Double.MAX_VALUE)
                {
                    terminals[r][c].setPotential(terminals[r][c].getPotential() - minVolts);
                }
            }
        }
        // Loop through any remaining components in componentsCopy. If their terminal potentials are still Double.MAX_VALUE,
        // set them to 0. These would be components not connected to any complete circuit.
        // This may not be correct, since these components may include a battery, or even a separate complete circuit...
        for (Component c : componentsCopy)
        {
            if (c.getEndPt1().getPotential() >= Double.MAX_VALUE / 10)
            {
                c.getEndPt1().setPotential(0);
                c.getEndPt2().setPotential(0);
            }
        }       
    }

    /**
     * Adds a component to a to a specific location in a circuit. Ordinarily this is performed only when first specifying 
     * the circuit. If a different component already exists at the specified location, this method has no effect. 
     * Updates the endpoints of the component so that it knows what terminals it is connected to. Updates those
     * terminals so that they know they are connected to this component. If the component is a battery, this method defaults to
     * making endPoint1 the positive terminal of the battery.
     * @param c  The component to be added
     * @param r1  Endpoint 1's row
     * @param c1  Endpoint 1's column
     * @param r2 Endpoint 2's row
     * @param c2  Endpoint 2's column
     * return  True if component is successfully added to circuit; false if the component was not added
     */
    public boolean addComponent(Component c, int r1, int c1, int r2, int c2)
    {
        c.setEndPt1(terminals[r1][c1]);
        c.setEndPt2(terminals[r2][c2]);
        // Check wheter a component already exists at this location
        if (findCorrespondingComponent(this, c) != null)
        {
            return false;
        }
        components.add(c);
        terminals[r1][c1].connect(c);
        terminals[r2][c2].connect(c);
        if (c instanceof Battery)
        {
            ((Battery)c).setPosEnd(terminals[r1][c1]);
        }
        return true;
    }

    /**
     * Like addComponent, but used only for batteries.
     * @param b  The battery to be added
     * @param r1  Endpoint 1's row
     * @param c1  Endpoint 1's column
     * @param r2 Endpoint 2's row
     * @param c2  Endpoint 2's column
     * @param posEndRow  Positive terminal's row
     * @param posEndCol  Positive terminal's column
     * return  True if component is successfully added to circuit; false if the component was not added
     */
    public boolean addBattery(Battery b, int r1, int c1, int r2, int c2, int posEndRow, int posEndCol)
    {
        b.setEndPt1(terminals[r1][c1]);
        b.setEndPt2(terminals[r2][c2]);
        // Check wheter a component already exists at this location
        if (findCorrespondingComponent(this, b) != null)
        {
            return false;
        }
        components.add(b);
        terminals[r1][c1].connect(b);
        terminals[r2][c2].connect(b);
        b.setPosEnd(terminals[posEndRow][posEndCol]);
        return true;
    }

    /**
     * Removes a component from a specified location within a circuit. This can be used in the process 
     * of desiging a circuit. It also gets used on a copy of the original circuit while identifying independent loops.
     * @param r1  One endpoint's row
     * @param c1  One endpoint's column
     * @param r2  The other endpoint's row
     * @param c2  The other endpoint's column
     */
    public void removeComponent(int r1, int c1, int r2, int c2)
    {
        Component c = getComponent(r1, c1, r2, c2);
        components.remove(c);
        c.setEndPt1(null);
        c.setEndPt2(null);
        terminals[r1][c1].disconnect(c);
        terminals[r2][c2].disconnect(c);
    }

    /**
     * Searches for the specified component and if found, removes it from the circuit. This can be used in the process
     * of desiging a circuit. It also gets used on a copy of the original circuit while identifying independent loops.
     * @param The component to be removed.
     */
    public void removeComponent(Component c)
    {
        c.getEndPt1().disconnect(c);
        c.getEndPt2().disconnect(c);
        components.remove(c);
        c.setEndPt1(null);
        c.setEndPt2(null);
    }

    /**
     * Returns the component from a specified location in a circuit.
     * @param r1  One endpoint's row
     * @param c1  One endpoint's column
     * @param r2  The other endpoint's row
     * @param c2  The other endpoint's column
     * @return  Returns the component found at that location or null if no component is at that location.
     */
    public Component getComponent(int r1, int c1, int r2, int c2)
    {
        for (Component c : components)
        {
            if ( c.getEndPt1().equals(terminals[r1][c1]) && c.getEndPt2().equals(terminals[r2][c2]) || c.getEndPt2().equals(terminals[r1][c1]) && c.getEndPt1().equals(terminals[r2][c2]) )
            {
                return c;
            }
        }
        return null;
    }

    /**
     * @param row  The row of the desired terminal
     * @param col  The column of the desired terminal.
     * @return  Returns the teminal at the specified location.
     */
    public Terminal getTerminal(int row, int col)
    {
        return terminals[row][col];
    }

    /**
     * @return  Returns the number of rows of terminals in the circuit
     */
    public int getRows()
    {
        return rows;
    }

    /**
     * @return  Returns the number of columns of terminals in the circuit
     */
    public int getCols()
    {
        return cols;
    }

    /**
     * @return  Returns the number independent branches in the circuit, excluding any branches that do not form complete circuits
     */
    public int getNumBranches()
    {
        return numBranches;
    }

    /**
     * @return  Returns a reference to the 2D array of terminals in the circuit
     */
    public Terminal[][] getTerminals()
    {
        return terminals;
    }

    /**
     * @return  Returns a reference to the ArrayList of components in the circuit
     */
    public List<Component> getComponents()
    {
        return components;
    }

    /**
     * @return  Returns a String list of the components within each branch of the circuit, including any dead-end branches
     */
    public String toString()
    {
        String result = "";
        boolean has999 = false;
        for (int branch = 0; branch < numBranches; branch++)
        {
            result += "Branch " + branch + "\n";
            for (Component comp : components)
            {
                if (comp.getBranch() == 999)
                {
                    has999 = true;
                }
                if (comp.getBranch() == branch)
                {
                    result += comp.toString() + "\n";
                }
            }
            result += "\n";
        }
        if (has999)
        {
            result += "Branch 999\n";
            for (Component comp : components)
            {
                if (comp.getBranch() == 999)
                {
                    result += comp.toString() + "\n";
                }
            }
        }
        result += "\n";
        return result;
    }
}

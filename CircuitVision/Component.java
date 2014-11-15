/**
 * All components (Resistors, Wires, Batteries, etc.) that can be placed between two terminals 
 * are subclasses of Component. When a component is added to a circuit, its endPt variables
 * are assigned Terminals.
 */
abstract public class Component
{
    protected int resistance;
    protected Terminal endPt1;
    protected Terminal endPt2;
    protected double current;
    protected int branch;
    protected Terminal currentDirection;    // this will equal either endPt1 or endPt2. Current flows from 
                                            // the other end, through component, and toward this terminal.

    /**
     * This constructor is called by all Component supclasses. When a component is added to a circuit,
     * it's endPt1 and endPt2 fields are set to the terminals at either end. The default resistance,
     * current, and voltage are set to 0. Branch is set to -1, and remains so until labelBranches()
     * is called by the circuit's solve() method.
     */
    public Component()
    {
        resistance = 0;
        current = 0.0;
        branch = -1;
    }

    /**
     * Accessor method called by the circuit's addComponent() method to set the component's endpoints.
     * @param p  The terminal connected to one end of the component.
     */
    public void setEndPt1(Terminal p)
    {
        endPt1 = p;
    }

    /**
     * Setter method called by the circuit's addComponent() method to set the component's endpoints.
     * @param p  The terminal connected to the other end of the component.
     */
    public void setEndPt2(Terminal p)
    {
        endPt2 = p;
    }

    /**
     * Accessor method for to get the terminal at one end of the component.
     */
    public Terminal getEndPt1()
    {
        return endPt1;
    }

    /**
     * Accessor method for to get the terminal at the other end of the component.
     */
    public Terminal getEndPt2()
    {
        return endPt2;
    }

    /**
     * @return  The component's resistance, which should be 0 for batteries and wires.
     */
    public int getResistance()
    {
        return resistance;
    }

    /**
     *  Sets a component's resistance. Used as components are added to a circuit.
     */
    public void setResistance(int r)
    {
        resistance = r;
    }
    
    /**
     * After a circuit has been solved, this method is used to update the components' currents.
     */
    public void setCurrent(double i)
    {
        current = i;
    }

    /**
     * @return  Returns the current passing through the component, as determined by solve().
     */
    public double getCurrent()
    {
        return current;
    }

    /**
     * Called by labelBranches() as part of the solve() method. The branch number corresponds to a current
     * variable: currents[branch].
     */
    public void setBranch(int b)
    {
        branch = b;
    }

    /**
     * @return  Which branch this component is a part of, and thus which current (current[branch])
     * is traveling through this component
     */
    public int getBranch()
    {
        return branch;
    }

    /**
     * Current direction through a component is specified by identifying the terminal at one end. The current
     * flows from the other end, through the component, and then through the currentDirection end.
     * Current directions are set at the same time branches are labeled within the solve() method.
     * @param endPt  The terminal at the end of the component where current exits the component
     */
    public void setCurrentDirection(Terminal endPt)
    {
        currentDirection = endPt;
    }

    /**
     * @return  The terminal at the end of the component where current exits the component
     */
    public Terminal getCurrentDirection()
    {
        return currentDirection;
    }

    /**
     * @return true if both "this" component and "other" are connected between the same terminals,
     * are both the same component sub-class, and have same value for Resistance or Voltage (if a battery).
     */
    public boolean equals(Component other)
    {
        if (endPt1 == null || other.getEndPt1() == null)
        {
            return false;
        }
        if (endPt1.equals(other.getEndPt1()) && endPt2.equals(other.getEndPt2()) || endPt1.equals(other.getEndPt2()) && endPt2.equals(other.getEndPt1()))
        {
            if (this instanceof Wire && other instanceof Wire)
            {
                return true;
            }
            else if (this instanceof Resistor && other instanceof Resistor && resistance == other.getResistance())
            {
                return true;
            }
            else if ( this instanceof Battery && other instanceof Battery && ((Battery)this).getVoltage() == ((Battery)other).getVoltage() && ((Battery)this).getPosEnd().equals( ((Battery)other).getPosEnd() ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return  Returns the terminals the component is connected between, the current direction terminal,
     * and the current through the component.
     */
    public String toString()
    {
        String result = "";
        if (endPt1 != null)
        {
            result += "(" + endPt1.getCol() + ", " + endPt1.getRow() + ") to ";
        }
        else
        {
            result += "null to ";
        }
        if (endPt2 != null)
        {
            result += "(" + endPt2.getCol() + ", " + endPt2.getRow() + ")  ";
        }
        else
        {
            result += "null  ";
        }
        result += "Current Direction: " + getCurrentDirection() + "  ";
        result += "Current: " + getCurrent() + "  ";
        return result;
    }
}

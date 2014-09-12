/**
 * Batteries provide a voltage and are assumed to have no internal resistance. One terminal
 * is marked as the positiveEnd.
 */
public class Battery extends Component
{
    private Terminal positiveEnd;
    private double battVoltage; // always positive
    
    /**
     * Constructs a Battery with the specified voltage. That voltage will be changed into a 
     * negative value when current directions are assigned if the current direction chosen is
     * such that it flows through the battery from its positive to its negative terminal.
     * @param volts  Voltage of the battery
     */
    public Battery(double volts)
    {
        super();
        battVoltage = volts;
    }
    
    /**
     * Mutator to set which end of a battery is the positive terminal.
     * @param posTerminal  The Terminal connected to the positive end of the battery.
     */
    public void setPosEnd(Terminal posTerminal)
    {
        positiveEnd = posTerminal;
    }
    
    /**
     * @return  Returns the Terminal connected to the positive end of the battery
     */
    public Terminal getPosEnd()
    {
        return positiveEnd;
    }
    
    /**
     * Typically used to set the voltage of a Battery, or by the solve() method once current through
     * each circuit branch has been determined.
     * @param  Voltage across a component.
     */
    public void setVoltage(double v)
    {
        battVoltage = v;
    }

    /**
     * Accessor for a component's voltage.
     * @return  Voltage across the component.
     */
    public double getVoltage()
    {
        return battVoltage;
    }

    
    /**
     * @return  Returns the Component's toString() with the battery's voltage and positive end appended
     */
    public String toString()
    {
        return super.toString() + "Battery " + battVoltage + " V   Pos. End " + getPosEnd() + "\t";
    }
}


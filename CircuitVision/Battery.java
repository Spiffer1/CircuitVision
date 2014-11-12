/**
 * Batteries provide a voltage and are assumed to have no internal resistance. One terminal
 * is marked as the positiveEnd.
 */
public class Battery extends Component
{
    private Terminal positiveEnd;
    private double battVoltage; // always positive
    
    /**
     * Constructs a Battery with the specified voltage.
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
     * @param  Voltage across a component.
     */
    public void setVoltage(double v)
    {
        battVoltage = v;
    }

    /**
     * Accessor for a battery's voltage.
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


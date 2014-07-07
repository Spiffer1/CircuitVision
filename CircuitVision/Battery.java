/**
 * Batteries provide a voltage and are assumed to have no internal resistance. One terminal
 * is marked as the positiveEnd.
 */
public class Battery extends Component
{
    private Terminal positiveEnd;
    
    /**
     * Constructs a Battery with the specified voltage. That voltage will be changed into a 
     * negative value when current directions are assigned if the current is direction chosen
     * such that it flows through the battery from its positive to its negative terminal.
     * @param volts  Voltage of the battery
     */
    public Battery(double volts)
    {
        super();
        voltage = volts;
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
     * @return  Returns the Component's toString() with the battery's voltage and positive end appended
     */
    public String toString()
    {
        return super.toString() + "Battery " + voltage + " V   Pos. End " + getPosEnd() + "\t";
    }
}


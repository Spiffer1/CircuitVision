/**
 * Components that are purely resistive.
 */
public class Resistor extends Component
{
    /**
     * Constructor sets the resistance to the specified value.
     * @param resist  Resistance of the resistor in ohms
     */
    public Resistor(int resist)
    {
        super();
        resistance = resist;
    }
    
    /**
     * @return Adds to the component's toString() the resistance value
     */
    public String toString()
    {
        return super.toString() + "Resistor " + resistance + " ohms\t";
    }
}

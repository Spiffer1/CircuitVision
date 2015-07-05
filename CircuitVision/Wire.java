/**
 * Wire has no voltage and no resistance.
 */
public class Wire extends Component
{
    /**
     * Constructs a piece of wire
     */
    public Wire()
    {
        super();
    }
    
    /**
     * @return  Returns the Component toString() with "Wire" appended.
     */
    public String toString()
    {
        return super.toString() + "Wire\t\t";
    }
}


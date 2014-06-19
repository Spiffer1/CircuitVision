import java.util.List;
import java.util.ArrayList;

public class Terminal
{
    private List<Component> connections;
    private int row;
    private int col;
    private double potential;

    /**
     * Constructs a Terminal and instantiates an arrayList to hold the Components the terminal 
     * is connected to. Initially, this arrayList is empty. The default potential for each terminal
     * is Double.MAX_VALUE. This can be used to test whether a given terminal has been assigned
     * a new potential value.
     * @param r  The row where the terminal is located
     * @param c  The column where the terminal is located.
     */
    public Terminal(int r, int c)
    {
        row = r;
        col = c;
        connections = new ArrayList<Component>();
        potential = Double.MAX_VALUE;
    }

    /**
     * @return  Returns true if this Terminal is connected to Component c; false otherwise.
     */
    public boolean connectedTo(Component c)
    {
        for (Component aConnectedComponent : connections) 
        {
            if (aConnectedComponent == c)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return  A reference to the ArrayList holding the components that the terminal is connected to
     */
    public List<Component> getConnections()
    {
        return connections;
    }

    /**
     * Returns an individual component that is connected to the Terminal.
     * @param i  The index of the component in the connections List to be returned
     * @return  Returns the i'th component in the connections List
     */
    public Component getConnection(int i)
    {
        return connections.get(i);
    }

    /**
     * Adds a component to the connections List. This method is called by the circuit's addComponent() method
     * @param c  The Component to be connected
     */
    public void connect(Component c)
    {
        connections.add(c);
    }

    /**
     * Removes the component from the connections List of this Terminal. Called by circuit's removeComponent() method
     * @param c  The Component to be disconnected.
     */
    public void disconnect(Component c)
    {
        connections.remove(c);
    }

    /**
     * @return  The number of components this Terminal is connected to
     */
    public int numConnections()
    {
        return connections.size();
    }

    /**
     * @return  The row where this terminal is located
     */
    public int getRow()
    {
        return row;
    }

    /**
     * @return  The column where this terminal is located
     */
    public int getCol()
    {
        return col;
    }

    /**
     * Sets the potential at this Terminal
     * @param p  The new potential at this terminal
     */
    public void setPotential(double p)
    {
        potential = p;
    }

    /**
     * @return  The potential at this terminal
     */
    public double getPotential()
    {
        return potential;
    }

    /**
     * Two Terminals are "equal" when their row and column match. Allows comparison of Terminals from
     * different circuits, using findCorrespondingComponent(), for example.
     */
    public boolean equals(Terminal other)
    {
        if (other != null && row == other.getRow() && col == other.getCol())
        {
            return true;
        }
        return false;
    }

    /**
     * @ return Returns the x and y coordinates of the Terminal. Note: it is not in row, column order.
     */
    public String toString()
    {
        return "(" + col + ", " + row + ")";
    }
}

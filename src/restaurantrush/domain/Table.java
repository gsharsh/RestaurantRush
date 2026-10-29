package restaurantrush.domain;

public final class Table {
    private final int id;
    private Customer occupant;

    public Table(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Table ID must be positive: " + id);
        }
        this.id = id;
        this.occupant = null;
    }

    public boolean isAvailable() {
        return occupant == null;
    }

    public void assign(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Cannot assign a null customer to table " + id);
        }
        if (!isAvailable()) {
            throw new IllegalStateException("Table " + id + " is already occupied by customer "
                    + occupant.getId());
        }
        this.occupant = customer;
    }

    public void release() {
        if (occupant == null) {
            throw new IllegalStateException("Table " + id + " is already free");
        }
        this.occupant = null;
    }

    public int getId() {
        return id;
    }

    public Customer getOccupant() {
        return occupant;
    }
}

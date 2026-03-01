package ee.cgi.broneerimine.domain;

import jakarta.persistence.*;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "tables")
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private int capacity;

    @Enumerated(EnumType.STRING)
    private Zone zone;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "table_features", joinColumns = @JoinColumn(name = "table_id"))
    @Column(name = "feature")
    private Set<Preference> features = EnumSet.noneOf(Preference.class);

    // koordinaadid ruudustikul
    private int x;
    private int y;

    public TableEntity() {}

    public Long getId() { return id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public Set<Preference> getFeatures() { return features; }
    public void setFeatures(Set<Preference> features) { this.features = features; }
}

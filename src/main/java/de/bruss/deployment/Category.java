package de.bruss.deployment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
public class Category implements Comparable<Category> {
    @Id
    @GeneratedValue
    private Long id;

    // general
    private String name;

    @ManyToOne
    private SortedSet<Config> configs = new TreeSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SortedSet<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(SortedSet<Config> configs) {
        this.configs = configs;
    }

    public Category(String name) {
        this.name = name;
    }

    public Category() {
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Category o) {
        return this.getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object obj) {
        return this.id != null && obj instanceof Category && ((Category) obj).getId() != null && id.equals(((Category) obj).getId());
    }
}

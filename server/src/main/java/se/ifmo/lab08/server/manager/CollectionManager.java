package se.ifmo.lab08.server.manager;

import se.ifmo.lab08.common.entity.Flat;
import se.ifmo.lab08.common.entity.Furnish;
import se.ifmo.lab08.common.entity.House;
import se.ifmo.lab08.server.persistance.repository.FlatRepository;
import se.ifmo.lab08.server.persistance.repository.HouseRepository;
import se.ifmo.lab08.server.persistance.repository.UserRepository;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class CollectionManager {
    private final Stack<Flat> collection;
    private final ZonedDateTime createdAt = ZonedDateTime.now();

    CollectionManager() {
        this.collection = new Stack<>();
    }

    public List<Flat> getCollection() {
        return collection.stream().toList();
    }

    public static CollectionManager fromDatabase() {
        var collection = new CollectionManager();
        var repository = new FlatRepository(new HouseRepository(), new UserRepository());
        try {
            for (Flat flat : repository.findAll()) {
                collection.push(flat);
            }
            return collection;
        } catch (SQLException e) {
            return new CollectionManager();
        }
    }


    public void push(Flat element) {
        if (element.validate() && get(element.getId()) == null) {
            collection.push(element);
        }
    }

    public void update(long id, Flat newFlat) {
        Flat flat = get(id);
        flat.update(newFlat);
    }

    public void removeById(long id) {
        collection.removeElement(get(id));
    }

    public List<Long> removeByOwnerId(int id) {
        List<Long> ids = collection.stream()
                .filter(flat -> flat.getOwner().getId().equals(id))
                .map(Flat::getId).collect(Collectors.toList());
        collection.removeIf(flat -> flat.getOwner().getId().equals(id));
        return ids;
    }

    public Flat last() {
        return collection.isEmpty() ? null : collection.peek();
    }

    public Flat get(long id) {
        for (Flat flat : collection) if (flat.getId() == id) return flat;
        return null;
    }

    public String description() {
        return String.format("Тип: %s\nДата инициализации: %s\nКол-во элементов: %s",
                collection.getClass().getName(), createdAt, collection.size());
    }

    public Flat min() {
        return collection.stream()
                .min(Flat::compareTo)
                .orElse(null);
    }

    public void shuffle() {
        Collections.shuffle(collection);
    }

    public List<Long> removeByFurnish(String username, Furnish furnish) {
        List<Long> ids = collection.stream()
                .filter(flat -> flat.getFurnish() == furnish && flat.getOwner().getUsername().equals(username))
                .map(Flat::getId).toList();
        collection.removeIf(flat -> flat.getFurnish() == furnish && flat.getOwner().getUsername().equals(username));
        return ids;
    }

    public List<Flat> filterByName(String name) {
        return collection.stream()
                .filter(flat -> flat.getName().toLowerCase().startsWith(name.toLowerCase()))
                .toList();
    }

    public Set<House> getUniqueHouses() {
        return collection.stream()
                .map(Flat::getHouse)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Flat flat : collection) {
            builder.append(flat);
            builder.append("\n");
        }
        return builder.toString();
    }
}

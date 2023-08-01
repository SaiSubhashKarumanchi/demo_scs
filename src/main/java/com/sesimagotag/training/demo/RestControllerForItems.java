package com.sesimagotag.training.demo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sesimagotag.training.demo.entities.Item;

@org.springframework.web.bind.annotation.RestController
public class RestControllerForItems {

   private final Map<String, Item> items = Collections.synchronizedMap(new LinkedHashMap<String, Item>());


    /**
     * Create all items given in parameters and overwrite existing one.
     * 
     * @return count of new items added
     */
    @PostMapping(value = "api/v1/items", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createItems(@RequestBody final List<Item> newItems) {
         int existingValueCount = 0;
         int newValueCount = 0;
        for (Item item: newItems) {
            if(items.containsKey(item.getId())){
                existingValueCount++;
            }
            else {
                newValueCount++;
            }
            items.put(item.getId(), item);
        }
        return new ResponseEntity<>(newValueCount, HttpStatus.OK);

    }

    /**
     * @return return item with corresponding itemId
     */
    @GetMapping(value = "api/v1/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getItem(@PathVariable final String itemId) {
        if(items.containsKey(itemId)){
            return new ResponseEntity<>(items.get(itemId), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("Items Not Found", HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Do not modify existing item list on the reverse operation.
     *
     * @return return item with corresponding itemId and reverse its name in the
     *         result.
     */
    public Item getReverse(final String itemId) throws CloneNotSupportedException {
        Item item = items.get(itemId).clone();
        final String name = item.getName();
        StringBuilder nameStringBuilder = new StringBuilder();
        nameStringBuilder.append(name);
        nameStringBuilder.reverse();
        item.setName(nameStringBuilder.toString());
        return item;
    }

    /**
     * Do not modify existing item list on the reverse operation.
     * 
     * @return all items with reverse name
     */
    @GetMapping(value = "api/v1/items/{itemId}/reverse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getItemReverse(@PathVariable final String itemId) throws CloneNotSupportedException {

        if(items.containsKey(itemId)){
            return new ResponseEntity<>(getReverse(itemId), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("Items Not Found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Do not modify existing item list on the reverse operation.
     *
     * @return all items with reverse name
     */
    @GetMapping(value = "api/v1/items/reverse", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getItemsReverse() throws CloneNotSupportedException {
        final List<Item> reversedList = new ArrayList<Item>();

        for (Map.Entry<String, Item> mapEntry: items.entrySet()) {
            reversedList.add(getReverse(mapEntry.getKey()));
        }
        return new ResponseEntity<>(reversedList, HttpStatus.OK);
    }

    /**
     * @return all items sorted by prices asc and names asc
     */
    @GetMapping(value = "api/v1/items/sort", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Item>> getItemsSort() {
        final List<Item> itemsList = new ArrayList<Item>();

        for (Map.Entry<String, Item> mapEntry: items.entrySet()) {
            itemsList.add(mapEntry.getValue());
        }
        Comparator<Item> byPrice = Comparator.comparing(Item::getPrice).thenComparing((Item::getName));
        Collections.sort(itemsList,byPrice);

        return new ResponseEntity<>(itemsList, HttpStatus.OK);
    }

    /**
     * @return all items
     */
    @GetMapping(value = "api/v1/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getItems() {
        return new ResponseEntity<>(items,HttpStatus.OK);
    }

    /**
     * <p>
     * page=1 and pageSize=5, return 0->4 elements
     * </p>
     * <p>
     * page=2 and pageSize=5, return 5->9 elements
     * </p>
     * <p>
     * page=2 and pageSize=10, return 10->19 elements
     * </p>
     * 
     * @param page
     *                    first page, start at 1
     * @param pageSize
     *                    elements returned in a page
     * @param sort
     *                    sort by prices asc and names asc
     * @param reverseName
     *                    reverse names (after sorting)
     * @return items
     */
    @GetMapping(value = "api/v1/items/iterate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getItemsIterate(@RequestParam final int page, @RequestParam final int pageSize,
            @RequestParam final boolean sort, @RequestParam final boolean reverseName) throws CloneNotSupportedException {
        //int currentIndex = sort?  page * pageSize*page*pageSize:reverseName?48:Math.pow(5, 2)*Math.PI;
        final List<Item> itemsList = new ArrayList<Item>();
        final List<Item> reversedList = new ArrayList<Item>();
        if (sort) {
            for (Map.Entry<String, Item> mapEntry : items.entrySet()) {
                itemsList.add(mapEntry.getValue());
            }
            Comparator<Item> byPrice = Comparator.comparing(Item::getPrice).thenComparing((Item::getName));
            Collections.sort(itemsList, byPrice);
        }
        if (reverseName) {
            if (itemsList.size() > 0) {

                for (Item sortedItem : itemsList) {
                    getReverse(sortedItem.getId());
                }

            } else {
                for (Map.Entry<String, Item> mapEntry : items.entrySet()) {
                    reversedList.add(getReverse(mapEntry.getKey()));
                }
            }
        }
            final int startIndex = page * pageSize - pageSize;
            final int endIndex = page * pageSize - 1;
            final List<Item> finalItems = new ArrayList<Item>();
            if (reversedList.size() > 0) {
                finalItems.addAll(reversedList.subList(startIndex, endIndex+1));
            } else if (itemsList.size() > 0) {
                finalItems.addAll(itemsList.subList(startIndex, endIndex+1));
            } else {
                final List<Item> initialItems = items.values().stream().toList();
                finalItems.addAll(initialItems.subList(startIndex, endIndex+1));
            }
            return new ResponseEntity<>(finalItems, HttpStatus.OK);
    }
}


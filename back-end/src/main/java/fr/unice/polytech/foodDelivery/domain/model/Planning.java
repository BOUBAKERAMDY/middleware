package fr.unice.polytech.foodDelivery.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Planning {
    private Map<String, TimeSlot> timeSlots;

    public Planning() {
        this.timeSlots = new HashMap<>();
    }

    public void addTimeSlot(Planning timeSlot) {
        for (Map.Entry<String, TimeSlot> entry : timeSlot.getTimeSlots().entrySet()) {
            this.timeSlots.put(entry.getKey(), entry.getValue());
        }
    }

    public void addTimeSlot(String slotId, DayOfWeek day, LocalTime startTime, LocalTime endTime, int capacity) {
        timeSlots.put(slotId, new TimeSlot(day, startTime, endTime, capacity));
    }

    public void updateCapacity(String slotId, int newCapacity) {
        TimeSlot slot = timeSlots.get(slotId);
        if (slot != null) {
            slot.setCapacity(newCapacity);
        }
    }

    public void removeTimeSlot(String slotId) {
        timeSlots.remove(slotId);
    }

    public boolean isAvailable(String slotId) {
        TimeSlot slot = timeSlots.get(slotId);
        return slot != null && slot.hasCapacity();
    }

    public Map<String, TimeSlot> getTimeSlots() {
        return new HashMap<>(timeSlots);
    }

    public TimeSlot getTimeSlot(String slotId) {
        return timeSlots.get(slotId);
    }

    @JsonIgnore
    public boolean isOpen() {
        LocalTime now = LocalTime.now();
        DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();

        return timeSlots.values().stream()
                .anyMatch(slot -> slot.day == today &&
                        !now.isBefore(slot.startTime) &&
                        !now.isAfter(slot.endTime));
    }

    public void displayAvailableSlots() {
        System.out.println("Available Time Slots:");
        for (Map.Entry<String, TimeSlot> entry : timeSlots.entrySet()) {
            TimeSlot slot = entry.getValue();
            if (slot.hasCapacity()) {
                System.out.println("Slot ID: " + entry.getKey() + ", From: " + slot.getStartTime() + " To: "
                        + slot.getEndTime() + ", Capacity: " + slot.getCapacity() + ", Current Orders: "
                        + slot.getCurrentOrders());
            }
        }
    }

    public static class TimeSlot {
        private DayOfWeek day;
        private LocalTime startTime;
        private LocalTime endTime;
        private int capacity;
        private int currentOrders;

        public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime, int capacity) {
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.capacity = capacity;
            this.currentOrders = 0;
        }

        public boolean hasCapacity() {
            return currentOrders < capacity;
        }

        public void incrementOrders() {
            if (hasCapacity()) {
                currentOrders++;
            }
        }

        public DayOfWeek getDay() {
            return day;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getCurrentOrders() {
            return currentOrders;
        }

        public void decrementOrders() {

        }
    }
}

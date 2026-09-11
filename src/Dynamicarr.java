import java.util.Arrays;

public class Dynamicarr<T> {
    private Object[] arr;
    private int size;
    int capacity=5;

    public Dynamicarr(int capacity) {
        this.capacity = capacity;
        arr=new Object[capacity];
        size=0;
    }

    public Dynamicarr() {
        arr=new Object[capacity];
        size=0;
    }

    public int getSize() {
        return capacity;
    }
    public int size(){
        return size;
    }

    public void add(T data){
        if(size==capacity){
            resize();
        }
        arr[size]=data;
        size++;

    }

    public T get(int index){
        checkBounds(index);
        return (T) arr[index];
    }

    public T remove(){
        if(size==0){
            System.out.println("[]");
            throw new RuntimeException("Array is empty");
        }
        T removed=(T) arr[size-1];
        arr[size-1]=null;
        size--;
        return removed;

    }

    private void resize(){
        int newCapacity=capacity*2;
        capacity=newCapacity;
        arr = Arrays.copyOf(arr, newCapacity);
    }

    public void checkBounds(int index){
        if(index<0||index>=size){
            throw new IndexOutOfBoundsException();
        }
    }
}

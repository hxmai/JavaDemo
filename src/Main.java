import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main(String[] args){
        User u = new User(){
            {
              this.name = "a";
            }
        };
        ThreadLocal threadLocal = new ThreadLocal();
        List list = new ArrayList(){
            {
            }
        };
    }
}

class User{
    public String name;
}

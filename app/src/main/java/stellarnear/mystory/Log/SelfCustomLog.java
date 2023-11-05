package stellarnear.mystory.Log;

public abstract class SelfCustomLog {
    public transient CustomLog log = new CustomLog(this.getClass());
}

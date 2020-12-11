package by.issoft.training;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    public static final double TIME_SCALE = 2; // try 10 and up for fun
    private static final int FLOOR_COUNT = 5;
    private static final int ELEVATOR_COUNT = 3;

    private static final long SPAWN_ORIGIN = 3;
    private static final long SPAWN_BOUND = 7;
    private static final TimeUnit SPAWN_UNIT = TimeUnit.SECONDS;

    private static final long UI_REDRAW_PERIOD = 100; // lower for quicker redraw. not affected by TIME_SCALE
    private static final TimeUnit UI_REDRAW_UNIT = TimeUnit.MILLISECONDS;

    public static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final ScheduledExecutorService logicExecutor = Executors.newScheduledThreadPool(8);
    private static final ScheduledExecutorService uiExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final StringBuilder uiBuilder = new StringBuilder();
    private static final Ansi ansi = new Ansi(uiBuilder);
    private static final StringBuilder sb = new StringBuilder();
    private static Terminal terminal;
    private static int lineCount;

    /**
     * Schedules command with adjustment to simulation time scale.
     */
    public static void schedule(Runnable command, long delay, TimeUnit unit) {
        logicExecutor.schedule(command, (long) (unit.toNanos(delay) / TIME_SCALE), TimeUnit.NANOSECONDS);
    }

    public static void main(String[] args) throws IOException {
        logger.info("log start");
        terminal = TerminalBuilder.terminal();
        
        ElevatorController ec = new ElevatorController(ELEVATOR_COUNT);
        PassengerController pc = new PassengerController(ec, FLOOR_COUNT);
        pc.startSpawning(SPAWN_ORIGIN, SPAWN_BOUND, SPAWN_UNIT);

        uiExecutor.scheduleAtFixedRate(/*redraw UI*/()->{
            // overdraw previous frame
            for (int i = 0; i < lineCount; i++) {
                ansi.cursorUpLine();
            }
            // draw elevator table
            uiAppendLine("ELEV STATE   FLOOR MASS DONE CALL ORDERS");
            for (int i = 0; i < ec.getElevators().size(); i++) {
                Elevator e = ec.getElevators().get(i);
                uiAppendLine(String.format("%-4s %-7s %-5s %-4s %-4s %-4s %s",
                    i,
                    e.getState(),
                    e.getCurrentFloor(),
                    e.getMass(),
                    e.getDone(),
                    e.getCall() == null ? "-" : e.getCall().toString(),
                    e.getOrders()
                ));
            }
            uiAppendLine("");

            // draw floor table
            sb.append("FLOOR");
            for (int i = 0; i < ELEVATOR_COUNT; i++) {
                sb.append(String.format(" %-2s", i));
            }
            sb.append(" QUEUE");
            uiAppendLine(sb);
            {
                int topFloor = FLOOR_COUNT - 1;
                sb.append(String.format("%-5s", topFloor));
                for (Elevator e : ec.getElevators()) {
                    sb.append(e.getCurrentFloor() == topFloor?" []":"   ");
                }
                sb.append(String.format(" ↓%s", pc.getFloorQueues()[topFloor].down()));
            }
            uiAppendLine(sb);
            for (int i = FLOOR_COUNT - 2; i > 0; i--) {
                sb.append(String.format("%-5s", i, pc.getFloorQueues()[i].up()));
                for (Elevator e : ec.getElevators()) {
                    sb.append(e.getCurrentFloor() == i?" []":"   ");
                }
                sb.append(String.format(" ↑%s", pc.getFloorQueues()[i].up()));
                uiAppendLine(sb);
                uiAppendLine(String.format("%-"+(5+3*ELEVATOR_COUNT)+"s ↓%s", "", pc.getFloorQueues()[i].down()));
            }
            sb.append(String.format("%-5s", 0));
            for (Elevator e : ec.getElevators()) {
                sb.append(e.getCurrentFloor() == 0?" []":"   ");
            }
            sb.append(String.format(" ↑%s", pc.getFloorQueues()[0].up()));
            uiAppendLine(sb);
        
            lineCount = ELEVATOR_COUNT+3+(FLOOR_COUNT-1)*2;
            AnsiConsole.out.print(ansi);
            uiBuilder.setLength(0);
        }, 0, UI_REDRAW_PERIOD, UI_REDRAW_UNIT);
    }

    /**
     * Trims and pads line to terminal width and appends it to UI string builder
     */
    private static void uiAppendLine(Object x) {
        String initial = x.toString();
        String trimmed = initial.substring(0, Math.min(terminal.getWidth(), initial.length()));
        String padded = String.format("%-"+terminal.getWidth()+"s", trimmed);
        uiBuilder.append(padded).append(System.lineSeparator());
    }

    private static void uiAppendLine(StringBuilder x) {
        uiAppendLine((Object)x);
        x.setLength(0);
    }
}

package ci583.collections;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * An implementation of the multi-level feedback queue
 * </p>
 * <p>
 * Following the chapter from
 * <a href="https://pages.cs.wisc.edu/~remzi/OSTEP/cpu-sched-mlfq.pdf">Link</a>
 * </p>
 * <p>
 *     Rules:
 *     <ol>
 *         <li>If Priority(A) > Priority(B), A runs (B doesn't)</li>
 *         <li>If Priority(A) = Priority(B), A & B run in round-robin fashion using the
 *         time-slice (quantum length) of the given queue</li>
 *         <li>When a job enters the system, it is placed at the highest priority (the
 *         uppermost queue)</li>
 *         <li>Once a job uses up it's time allotment at the given level (regardless of how
 *         many times it has given up the CPU), its priority is reduced (moves down to a lower
 *         queue)</li>
 *         <li>After some time period S, move all jobs in the system to the uppermost queue</li>
 *     </ol>
 * </p>
 *
 */
public class MLFQueue<T> {
}

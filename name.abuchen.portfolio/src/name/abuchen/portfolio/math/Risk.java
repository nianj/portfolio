package name.abuchen.portfolio.math;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.function.Predicate;

import name.abuchen.portfolio.util.Interval;

public final class Risk
{

    public static class Drawdown
    {
        private double maxDD;
        private Interval maxDDDuration;
        private Interval intervalMaxDD;

        public Drawdown(double[] values, Date[] dates)
        {
            double peak = values[0] + 1;
            Instant lastPeakDate = dates[0].toInstant();

            maxDD = 0;
            intervalMaxDD = Interval.of(lastPeakDate, lastPeakDate);
            maxDDDuration = Interval.of(lastPeakDate, lastPeakDate);
            Interval currentDrawdownDuration = null;

            for (int ii = 0; ii < values.length; ii++)
            {
                double value = values[ii] + 1;
                currentDrawdownDuration = Interval.of(lastPeakDate, dates[ii].toInstant());

                if (value > peak)
                {
                    peak = value;
                    lastPeakDate = dates[ii].toInstant();

                    if (currentDrawdownDuration.isLongerThan(maxDDDuration))
                        maxDDDuration = currentDrawdownDuration;
                }
                else
                {
                    double drawdown = (peak - value) / peak;
                    if (drawdown > maxDD)
                    {
                        maxDD = drawdown;
                        intervalMaxDD = Interval.of(lastPeakDate, dates[ii].toInstant());
                    }
                }
            }

            // check if current drawdown duration is longer than the max
            // drawdown duration currently calculated --> use it because it is
            // the longest duration even if we do not know how much longer it
            // will get

            if (currentDrawdownDuration != null && currentDrawdownDuration.isLongerThan(maxDDDuration))
                maxDDDuration = currentDrawdownDuration;
        }

        public double getMaxDrawdown()
        {
            return maxDD;
        }

        public Interval getIntervalOfMaxDrawdown()
        {
            return intervalMaxDD;
        }

        public Interval getMaxDrawdownDuration()
        {
            return maxDDDuration;
        }
    }

    public static class Volatility
    {
        private final double stdDeviation;
        private final double semiDeviation;

        public Volatility(double[] returns, Predicate<Integer> filter)
        {
            Objects.requireNonNull(returns);

            double averageReturn = average(returns, filter);
            double tempStandard = 0;
            double tempSemi = 0;
            int count = 0;

            for (int ii = 0; ii < returns.length; ii++)
            {
                if (!filter.test(ii))
                    continue;

                double add = Math.pow(returns[ii] - averageReturn, 2);

                tempStandard = tempStandard + add;
                count++;

                if (returns[ii] < averageReturn)
                    tempSemi = tempSemi + add;
            }

            stdDeviation = Math.sqrt(tempStandard / count);
            semiDeviation = Math.sqrt(tempSemi / count);
        }

        private double average(double[] returns, Predicate<Integer> filter)
        {
            double sum = 0;
            int count = 0;

            for (int ii = 0; ii < returns.length; ii++)
            {
                if (!filter.test(ii))
                    continue;

                sum += returns[ii];
                count++;
            }

            return sum / count;
        }

        public double getStandardDeviation()
        {
            return stdDeviation;
        }

        public double getSemiDeviation()
        {
            return semiDeviation;
        }

        public double getExpectedSemiDeviation()
        {
            return stdDeviation / Math.sqrt(2);
        }

        public String getNormalizedSemiDeviationComparison()
        {
            double expectedSemiDeviation = getExpectedSemiDeviation();
            if (expectedSemiDeviation > semiDeviation)
                return ">"; //$NON-NLS-1$
            else if (expectedSemiDeviation < semiDeviation)
                return "<"; //$NON-NLS-1$
            return "="; //$NON-NLS-1$
        }
    }

    private Risk()
    {}
}

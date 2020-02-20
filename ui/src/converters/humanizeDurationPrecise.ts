import moment from "moment";

moment.locale("precise-en", {
    relativeTime: {
        future : "%s",
        past : "%s ago",
        s : "%d seconds",
        m : "%d minute",
        mm : "%d minutes",
        h : "%d hour",
        hh : "%d hours",
        d : "%d day",
        dd : "%d days",
        M : "%d month",
        MM : "%d months",
        y : "%d year",
        yy : "%d years"
    }
});

export function humanizeDurationPrecise(duration: moment.Duration): string {
    return duration.locale("precise-en").humanize(true);
}
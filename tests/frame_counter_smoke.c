#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <time.h>
#include "frame_counter.h"

static void pause_ms(long ms) {
    struct timespec ts = { .tv_sec = 0, .tv_nsec = ms * 1000000L };
    nanosleep(&ts, 0);
}
static uint32_t sample_count(void) {
    uint32_t sum = 0;
    for (unsigned i = 0; i < CLEAVE_FRAME_TIME_BUCKETS; i++) sum += cleave_frame_time_bucket(i);
    return sum;
}
int main(void) {
    cleave_record_frame();
    assert(cleave_frames_available() && cleave_frame_count() == 1 && sample_count() == 0);
    cleave_set_frame_timing(1);
    cleave_record_frame();
    pause_ms(5);
    cleave_record_frame();
    assert(sample_count() == 1);
    cleave_set_frame_timing(0);
    pause_ms(5);
    cleave_record_frame();
    assert(sample_count() == 1);
    cleave_set_frame_timing(1);
    cleave_record_frame();
    assert(sample_count() == 1); /* no gap counted when resuming */
    pause_ms(5);
    cleave_record_frame();
    assert(sample_count() == 2 && cleave_frame_count() == 6);
    puts("Frame counter smoke tests passed");
}

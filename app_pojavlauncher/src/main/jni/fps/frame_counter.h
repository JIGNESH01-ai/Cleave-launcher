#ifndef CLEAVE_FRAME_COUNTER_H
#define CLEAVE_FRAME_COUNTER_H
#include <stdint.h>
#define CLEAVE_FRAME_TIME_BUCKETS 101
__attribute__((visibility("default"))) void cleave_record_frame(void);
__attribute__((visibility("default"))) uint32_t cleave_frame_count(void);
__attribute__((visibility("default"))) int cleave_frames_available(void);
__attribute__((visibility("default"))) void cleave_set_frame_timing(int enabled);
__attribute__((visibility("default"))) uint32_t cleave_frame_time_bucket(unsigned bucket);
#endif

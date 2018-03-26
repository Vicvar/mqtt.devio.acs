#!/usr/bin/env python
import sys
from datetime import datetime

acstime=long(sys.argv[1])
epoch=(acstime-122192928000000000.0)/10000000.0
dt=datetime.fromtimestamp(epoch)
ret=dt.strftime('%Y-%m-%dT%H:%M:%S.%f')
print ret

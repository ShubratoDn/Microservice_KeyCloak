/// <reference types="@testing-library/jest-dom/vitest" />
import * as matchers from '@testing-library/jest-dom/matchers'

// Adds matchers like toBeInTheDocument() to the global expect.
expect.extend(matchers)

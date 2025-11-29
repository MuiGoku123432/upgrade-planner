# Builds Page Design Document
## VIN Build Planner Service

**Version:** 1.0  
**Date:** November 27, 2024  
**Author:** Claude Code (AI Assistant)

---

## 1. Executive Summary

The Builds page is the primary workspace for users to manage their vehicle upgrade projects. It serves as the central hub where users can:

- View and manage their vehicle inventory
- Create and organize multiple build plans per vehicle
- Add and prioritize parts with detailed information
- Track costs, status, and purchase planning
- Organize parts by categories and tiers

This page leverages the hierarchical structure: **Vehicle → Builds → Parts → Sub-Parts**, enabling users to plan complex vehicle modifications with granular detail and cost tracking.

---

## 2. User Journey & Workflows

### 2.1 Primary User Flow

```
1. User navigates to /builds
2. Views their vehicle inventory
3. Selects a vehicle OR creates new vehicle
4. Views builds for that vehicle
5. Creates new build OR selects existing build
6. Manages parts within the build:
   - Add new parts
   - Update priorities
   - Set status (PLANNED → ORDERED → INSTALLED)
   - Add sub-parts for detailed breakdown
7. Reviews cost summaries and planning data
```

### 2.2 Vehicle Management Workflow

**Add New Vehicle:**
1. Click "Add Vehicle" button
2. Enter VIN (optional for project builds)
3. If VIN provided → Auto-populate via VIN decode
4. Add nickname, notes, and manual overrides
5. Save vehicle

**VIN-less Project Vehicle:**
1. Skip VIN field
2. Manually enter make/model/year (optional)
3. Add descriptive nickname (e.g., "Future 80-Series Build")
4. Save as project vehicle

### 2.3 Build Management Workflow

**Create New Build:**
1. Select vehicle
2. Choose upgrade category (Overlanding, Performance, etc.)
3. Name the build (e.g., "Overland Build v1")
4. Set priority level and target completion date
5. Save build

**Manage Parts:**
1. Select build
2. Add parts with category, tier, priority
3. Set pricing and purchase planning dates
4. Add sub-parts for detailed breakdown
5. Update status as progress occurs

---

## 3. Page Sections & Components

### 3.1 Page Layout Structure

```
┌─────────────────────────────────────────────────────────────┐
│ Navigation Bar (inherited)                                  │
├─────────────────────────────────────────────────────────────┤
│ Page Header                                                 │
│ ┌─ "Vehicle Builds" title                                   │
│ ├─ User greeting with vehicle count                         │
│ └─ Primary action: "Add Vehicle" button                     │
├─────────────────────────────────────────────────────────────┤
│ Vehicle Selector Section                                    │
│ ┌─ Vehicle tabs/cards (horizontally scrollable)             │
│ ├─ Each shows: nickname, make/model/year, build count       │
│ └─ Add vehicle card at end                                  │
├─────────────────────────────────────────────────────────────┤
│ Build Management Section (dynamic content area)            │
│ ┌─ Selected vehicle header with details                     │
│ ├─ Build tabs/cards for selected vehicle                    │
│ ├─ "Add Build" button                                       │
│ └─ Selected build details panel                            │
├─────────────────────────────────────────────────────────────┤
│ Parts Management Section (dynamic content area)            │
│ ┌─ Build summary (cost, status, progress)                   │
│ ├─ Parts filter/sort controls                              │
│ ├─ Parts table/list with expand/collapse for sub-parts     │
│ └─ "Add Part" button and quick actions                     │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Vehicle Selector Component

**Layout:** Horizontal scrollable cards
```html
<div class="vehicle-selector">
  <div class="vehicle-card" data-vehicle-id="{uuid}">
    <div class="vehicle-info">
      <h4>Trail Rig</h4>
      <p>2021 Toyota 4Runner TRD Off Road</p>
      <span class="build-count">3 builds</span>
    </div>
  </div>
  <div class="vehicle-card add-vehicle">
    <div class="add-icon">+</div>
    <span>Add Vehicle</span>
  </div>
</div>
```

**HTMX Integration:**
- Click vehicle card: `hx-get="/builds/{vehicleId}" hx-target="#build-section"`
- Click add vehicle: `hx-get="/vehicles/new" hx-target="#modal-container"`

### 3.3 Build Management Component

**Build Cards Layout:**
```html
<div class="build-selector">
  <div class="build-card" data-build-id="{uuid}">
    <h5>Overland Build v1</h5>
    <div class="build-meta">
      <span class="category">Overlanding</span>
      <span class="status">IN_PROGRESS</span>
      <span class="cost">$7,200</span>
    </div>
    <div class="build-progress">
      <div class="progress-bar" style="width: 65%"></div>
      <span>8 of 12 parts</span>
    </div>
  </div>
</div>
```

**HTMX Integration:**
- Click build card: `hx-get="/builds/{buildId}/parts" hx-target="#parts-section"`
- Add build: `hx-get="/builds/new?vehicleId={id}" hx-target="#modal-container"`

### 3.4 Parts Management Component

**Parts Table with Hierarchy:**
```html
<div class="parts-container">
  <div class="parts-controls">
    <div class="filters">
      <select name="category">...</select>
      <select name="tier">...</select>
      <select name="status">...</select>
    </div>
    <div class="sort-controls">
      <button data-sort="priority">Priority</button>
      <button data-sort="cost">Cost</button>
      <button data-sort="date">Target Date</button>
    </div>
  </div>
  
  <div class="parts-table">
    <div class="part-row" data-part-id="{uuid}">
      <div class="part-info">
        <h6>OME BP-51 Suspension Kit</h6>
        <div class="part-meta">
          <span class="priority">Priority 1</span>
          <span class="category">Suspension</span>
          <span class="tier">Premium</span>
          <span class="cost">$2,800</span>
        </div>
      </div>
      <div class="part-actions">
        <button class="expand-subparts">+</button>
        <button class="edit-part">✏️</button>
      </div>
    </div>
    
    <!-- Sub-parts (initially hidden) -->
    <div class="sub-parts" data-parent-id="{uuid}" style="display: none;">
      <div class="sub-part-row">
        <span class="sub-indicator">└─</span>
        <span class="sub-name">Front Shock Assembly</span>
        <span class="sub-cost">$600</span>
      </div>
    </div>
  </div>
</div>
```

---

## 4. Data Flow & API Integration

### 4.1 API Endpoints Mapping

**Vehicle Operations:**
- `GET /vehicles` → Load user's vehicles for selector
- `GET /vehicles/{id}` → Get vehicle details
- `POST /vehicles` → Create new vehicle
- `POST /vin/decode` → VIN decoding for auto-fill

**Build Operations:**
- `GET /vehicles/{vehicleId}/builds` → Load builds for selected vehicle
- `GET /builds/{buildId}` → Get build details
- `POST /vehicles/{vehicleId}/builds` → Create new build
- `PATCH /builds/{buildId}` → Update build

**Parts Operations:**
- `GET /builds/{buildId}/parts` → Load parts for selected build
- `POST /builds/{buildId}/parts` → Create new part
- `PATCH /parts/{partId}` → Update part
- `GET /parts/{partId}/sub-parts` → Load sub-parts
- `POST /parts/{partId}/sub-parts` → Create sub-part

**Lookup Data:**
- `GET /upgrade-categories` → Build categories
- `GET /part-categories` → Part categories
- `GET /part-tiers` → Part tiers

### 4.2 Data Flow Patterns

**Page Load Sequence:**
1. Load user's vehicles → populate vehicle selector
2. Auto-select first vehicle OR remember last selected
3. Load builds for selected vehicle
4. Load parts for selected build (if any)
5. Load lookup data for filters

**Dynamic Updates:**
- Vehicle selection triggers build loading
- Build selection triggers parts loading
- Part expansion triggers sub-parts loading
- Filter changes trigger parts re-filtering

---

## 5. HTMX Implementation Patterns

### 5.1 Progressive Loading

**Vehicle Selection Pattern:**
```html
<div class="vehicle-card" 
     hx-get="/builds?vehicleId={id}" 
     hx-target="#build-section" 
     hx-indicator="#loading-builds">
```

**Build Selection Pattern:**
```html
<div class="build-card" 
     hx-get="/builds/{id}/parts" 
     hx-target="#parts-section"
     hx-indicator="#loading-parts">
```

### 5.2 Modal Forms

**Add Vehicle Modal:**
```html
<button hx-get="/modals/add-vehicle" 
        hx-target="#modal-container" 
        hx-swap="innerHTML">Add Vehicle</button>
```

**VIN Decode Integration:**
```html
<input name="vin" 
       hx-post="/vin/decode" 
       hx-target="#vehicle-details" 
       hx-trigger="blur delay:500ms"
       hx-indicator="#vin-loading">
```

### 5.3 Inline Editing

**Part Status Updates:**
```html
<select name="status" 
        hx-patch="/parts/{id}" 
        hx-target="closest .part-row" 
        hx-swap="outerHTML">
  <option value="PLANNED">Planned</option>
  <option value="ORDERED">Ordered</option>
  <option value="INSTALLED">Installed</option>
</select>
```

### 5.4 Dynamic Filtering

**Parts Filter Pattern:**
```html
<select name="category" 
        hx-get="/builds/{buildId}/parts" 
        hx-target="#parts-table" 
        hx-include="[name='tier'], [name='status']"
        hx-trigger="change">
```

---

## 6. State Management

### 6.1 URL State

**Route Pattern:** `/builds?vehicle={id}&build={id}&view={parts|summary}`

**State Persistence:**
- Selected vehicle ID in URL
- Selected build ID in URL  
- Current view/tab in URL
- Filter states in URL parameters

### 6.2 Local State

**Browser Storage:**
- Recently selected vehicles (localStorage)
- User preferences (sort order, view mode)
- Draft form data for modals

**Server-Side State:**
- User session authentication
- Database state for all entities
- No complex state machines needed

---

## 7. Error Handling & Validation

### 7.1 Form Validation

**VIN Validation:**
- Client-side: 17-character format check
- Server-side: VIN decode API validation
- Error display: Inline under VIN field

**Required Fields:**
- Vehicle: nickname (if no VIN provided)
- Build: name, category
- Part: name, category

### 7.2 API Error Handling

**Network Errors:**
```html
<div hx-on::error="showErrorToast('Network error occurred')">
```

**Validation Errors:**
- Display field-level errors inline
- Show general error message at form level
- Prevent form submission until resolved

**Loading States:**
```html
<div class="loading-indicator" id="loading-parts">
  <span>Loading parts...</span>
</div>
```

---

## 8. Performance Considerations

### 8.1 Lazy Loading

**Hierarchical Loading:**
- Load vehicles first
- Load builds only when vehicle selected
- Load parts only when build selected
- Load sub-parts only when part expanded

**Pagination:**
- Parts table pagination for builds with >50 parts
- Virtual scrolling for vehicles if >20 vehicles

### 8.2 Caching Strategy

**Client-Side:**
- Cache lookup data (categories, tiers)
- Cache vehicle list for session
- Smart cache invalidation on updates

**Server-Side:**
- Cache user's vehicle list
- Cache lookup tables
- Optimize database queries with proper indexes

### 8.3 Network Optimization

**Request Batching:**
- Load parts with sub-part counts in single request
- Include related data in responses (category names, etc.)

**Minimal Updates:**
- HTMX swaps only changed elements
- Status updates replace single row, not entire table
- Modal forms don't reload page content

---

## 9. Security Considerations

### 9.1 Authorization

**Resource Access:**
- Users can only see their own vehicles/builds
- Server validates ownership on all operations
- Frontend hides unavailable actions

### 9.2 Input Validation

**Data Sanitization:**
- All form inputs validated server-side
- XSS prevention through proper templating
- SQL injection prevention through parameterized queries

### 9.3 CSRF Protection

**Form Protection:**
- All forms include CSRF tokens
- HTMX configured to send CSRF headers
- Server validates all state-changing operations

---

## 10. Accessibility & UX

### 10.1 Accessibility

**Keyboard Navigation:**
- Tab order through vehicle cards, builds, parts
- Enter/Space to activate cards and buttons
- Arrow keys for table navigation

**Screen Reader Support:**
- Proper ARIA labels for dynamic content
- Status announcements for updates
- Semantic HTML structure

### 10.2 Responsive Design

**Mobile Layout:**
- Stack vehicle cards vertically on small screens
- Collapsible sections for builds/parts
- Touch-friendly button sizes

**Progressive Enhancement:**
- Works without JavaScript (basic form submissions)
- HTMX enhances with dynamic loading
- Graceful degradation for network issues

---

## 11. Testing Strategy

### 11.1 Unit Testing

**Frontend Components:**
- Vehicle selector behavior
- Parts table filtering/sorting
- Form validation logic

**Backend Services:**
- API endpoint responses
- Database operations
- VIN decoding integration

### 11.2 Integration Testing

**User Workflows:**
- Complete vehicle creation flow
- Build management flow
- Parts addition and management

### 11.3 Performance Testing

**Load Testing:**
- Multiple concurrent users
- Large datasets (100+ vehicles, 1000+ parts)
- API response times under load

---

## 12. Future Enhancements

### 12.1 Phase 2 Features

**Advanced Filtering:**
- Date range filtering for purchase planning
- Cost range filtering
- Multi-select category/tier filters

**Import/Export:**
- CSV export of parts lists
- Import from vendor catalogs
- Backup/restore functionality

### 12.2 Phase 3 Features

**Collaboration:**
- Share builds with other users
- Comments and notes on parts
- Build templates and sharing

**Integrations:**
- Vendor API integrations for pricing
- Shopping cart functionality
- Installation progress tracking

---

## 13. Implementation Timeline

### 13.1 Phase 1 (Core MVP) - 2-3 weeks

**Week 1:**
- Basic page structure and navigation
- Vehicle management (CRUD)
- VIN decoding integration

**Week 2:**
- Build management (CRUD)
- Basic parts management
- Cost calculations

**Week 3:**
- Sub-parts functionality
- Filtering and sorting
- Polish and testing

### 13.2 Phase 2 (Enhanced Features) - 1-2 weeks

- Advanced filtering options
- Improved UX and responsiveness
- Performance optimizations
- Comprehensive testing

---

This design document serves as the comprehensive blueprint for implementing the builds page, incorporating all the database design, API specifications, and project requirements into a cohesive user experience.